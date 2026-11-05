package org.opendatamesh.cli.usecases.devops.starttask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.registry.RegistryValidationState;
import org.opendatamesh.cli.exceptions.BadRequestException;
import org.opendatamesh.cli.exceptions.NotFoundException;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.FakeClock;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.FakeDevOps;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.FakeRegistry;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.RecordingPresenter;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opendatamesh.cli.clients.platform.devops.ExecutionStatus.CANCELED;
import static org.opendatamesh.cli.clients.platform.devops.ExecutionStatus.FAILED;
import static org.opendatamesh.cli.clients.platform.devops.ExecutionStatus.PENDING;
import static org.opendatamesh.cli.clients.platform.devops.ExecutionStatus.RUNNING;
import static org.opendatamesh.cli.clients.platform.devops.ExecutionStatus.SUCCEEDED;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.FQN;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.VERSION;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.activity;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.command;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.task;

/**
 * Unit tests of the start-task use case with in-memory ports and a fake clock that advances on sleep.
 * Generated from BDMD-5443, prompt {@code spdd/prompt/BDMD-5443-202610291156-[Feat]-commands-devops-v2-start-complete-task.md}.
 */
class StartTaskTest {

    private FakeDevOps devOps;
    private FakeRegistry registry;
    private RecordingPresenter presenter;

    @BeforeEach
    void setUp() {
        devOps = new FakeDevOps();
        registry = new FakeRegistry();
        presenter = new RecordingPresenter();
    }

    private void run(StartTaskCommand command) {
        new StartTask(command, presenter, devOps, registry, new StartTaskDescriptorOutboundPortImpl(), new FakeClock()).execute();
    }

    private static DevOpsActivity summary(String uuid) {
        return activity(uuid, RUNNING);
    }

    /**
     * Scenario: First task of a stage creates the activity and starts the task.
     * Given no open activity for the data product version and stage
     * And the version is APPROVED in Registry and its stage lists the task
     * When the pipeline runs start-task with the coordinates and its run id
     * Then the activity is executed from the published descriptor
     * And the command waits for the activity and the task to be RUNNING
     * And the command exits 0
     */
    @Test
    void whenNoOpenActivityThenExecutesPlannedActivityAndStartsTask() {
        devOps.creation = new ActivityCreation.Created(activity("a1", PENDING, task("build", PENDING), task("lint", PENDING)));
        devOps.read("a1",
                activity("a1", PENDING, task("build", PENDING), task("lint", PENDING)),
                activity("a1", RUNNING, task("build", PENDING), task("lint", PENDING)),
                activity("a1", RUNNING, task("build", RUNNING), task("lint", PENDING)));

        run(command(false));

        assertThat(presenter.failure).isNull();
        assertThat(devOps.executedPlan.name()).isEqualTo("dev");
        assertThat(devOps.executedPlan.dataProductVersionUuid()).isEqualTo("version-uuid");
        assertThat(devOps.executedPlan.tasks()).extracting("name").containsExactly("build", "lint");
        assertThat(devOps.calls).containsSubsequence("find-open", "execute", "get:a1", "get:a1", "request:build:run-1", "get:a1");
        assertThat(presenter.startedTask.name()).isEqualTo("build");
        assertThat(presenter.startedTask.status()).isEqualTo(RUNNING);
        assertThat(presenter.startedActivity.status()).isEqualTo(RUNNING);
    }

    /**
     * Scenario: A later task reuses the open activity without calling Registry.
     * Given an open RUNNING activity whose next PENDING task is the requested one
     * When the pipeline runs start-task for that task
     * Then Registry is not called and the task is requested with the run id
     * And the command exits 0
     */
    @Test
    void whenOpenRunningActivityThenRequestsTaskWithoutRegistry() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1",
                activity("a1", RUNNING, task("prepare", SUCCEEDED), task("build", PENDING)),
                activity("a1", RUNNING, task("prepare", SUCCEEDED), task("build", PENDING)),
                activity("a1", RUNNING, task("prepare", SUCCEEDED), task("build", RUNNING)));

        run(command(false));

        assertThat(presenter.failure).isNull();
        assertThat(registry.calls).isZero();
        assertThat(devOps.calls).contains("request:build:run-1").doesNotContain("execute");
        assertThat(presenter.startedTask.name()).isEqualTo("build");
    }

    /**
     * Scenario: Activity rejected by policy.
     * Given the created activity becomes FAILED while waiting for approval
     * When start-task waits for the activity
     * Then the command exits 1 naming the rejection and the step AWAIT_ACTIVITY_APPROVAL
     * And no task is requested
     */
    @Test
    void whenActivityFailsDuringApprovalThenFailsWithoutRequest() {
        devOps.creation = new ActivityCreation.Created(activity("a1", PENDING, task("build", PENDING)));
        devOps.read("a1", activity("a1", PENDING, task("build", PENDING)), activity("a1", FAILED, task("build", FAILED)));

        run(command(false));

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class).hasMessageContaining("Activity a1 was rejected (FAILED)");
        assertThat(presenter.failedSteps).containsExactly(StartTaskStep.AWAIT_ACTIVITY_APPROVAL);
        assertThat(devOps.calls).noneMatch(call -> call.startsWith("request:"));
    }

    /**
     * Scenario: Task rejected by policy.
     * Given the requested task becomes FAILED while waiting for approval
     * When start-task waits for the task
     * Then the command exits 1 naming the task rejection
     */
    @Test
    void whenTaskFailsDuringApprovalThenFails() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1",
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", FAILED, task("build", FAILED)));

        run(command(false));

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Task build was rejected (FAILED); the activity is now FAILED");
        assertThat(presenter.failedSteps).containsExactly(StartTaskStep.AWAIT_TASK_APPROVAL);
    }

    /**
     * Scenario: Approval wait times out.
     * Given the activity stays PENDING longer than the activity timeout
     * When start-task waits for approval
     * Then the command exits 1 naming the timeout and writes nothing more to DevOps
     */
    @Test
    void whenActivityStaysPendingPastTimeoutThenFailsWithTimeout() {
        devOps.creation = new ActivityCreation.Created(activity("a1", PENDING, task("build", PENDING)));
        devOps.read("a1", activity("a1", PENDING, task("build", PENDING)));
        StartTaskCommand command = new StartTaskCommand(FQN, VERSION, "dev", "build", "run-1", false,
                Duration.ofSeconds(10), Duration.ofSeconds(30), Duration.ofMinutes(10));

        run(command);

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Timed out after PT30S waiting for activity a1 approval (still PENDING)");
        assertThat(presenter.failedSteps).containsExactly(StartTaskStep.AWAIT_ACTIVITY_APPROVAL);
        assertThat(devOps.calls).containsOnlyOnce("execute")
                .noneMatch(call -> call.startsWith("request:") || call.startsWith("status:") || call.startsWith("cancel:"));
    }

    /**
     * Scenario: Concurrent creation reuses the other run's activity.
     * Given Execute Activity is refused because the activity is already PENDING or RUNNING
     * When start-task handles the refusal
     * Then it searches the open activity again and continues with it
     */
    @Test
    void whenExecuteReportsAlreadyOpenThenReusesFoundActivity() {
        devOps.creation = new ActivityCreation.AlreadyOpen();
        devOps.openSearches.add(List.of());
        devOps.openSearches.add(List.of(summary("a2")));
        devOps.read("a2",
                activity("a2", RUNNING, task("build", PENDING)),
                activity("a2", RUNNING, task("build", PENDING)),
                activity("a2", RUNNING, task("build", RUNNING)));

        run(command(false));

        assertThat(presenter.failure).isNull();
        assertThat(devOps.calls).containsSubsequence("find-open", "execute", "find-open", "get:a2", "request:build:run-1");
        assertThat(presenter.startedActivity.uuid()).isEqualTo("a2");
    }

    /**
     * Scenario: Version not APPROVED is refused without the flag.
     * Given the version is PENDING in Registry
     * When start-task runs without --force-start
     * Then the command exits 1 naming the state and the --force-start flag
     * And DevOps is not called
     */
    @Test
    void whenVersionPendingWithoutForceThenFailsBeforeDevOps() {
        registry.state = RegistryValidationState.PENDING;

        run(command(false));

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Version 1.2.0 of urn:dp:sales is PENDING in Registry, not APPROVED. Pass --force-start to run it anyway.");
        assertThat(devOps.calls).containsExactly("find-open");
    }

    /**
     * Scenario: Version not APPROVED runs with the flag.
     * Given the version is REJECTED in Registry
     * When start-task runs with --force-start
     * Then a warning is logged and the activity is executed
     */
    @Test
    void whenVersionRejectedWithForceThenWarnsAndExecutes() {
        registry.state = RegistryValidationState.REJECTED;
        devOps.creation = new ActivityCreation.Created(activity("a1", PENDING, task("build", PENDING)));
        devOps.read("a1",
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", RUNNING, task("build", RUNNING)));

        run(command(true));

        assertThat(presenter.failure).isNull();
        assertThat(presenter.warnings).containsExactly("--force-start: running version 1.2.0 of urn:dp:sales in state REJECTED");
        assertThat(devOps.calls).contains("execute");
    }

    /**
     * Scenario: Version missing from Registry fails even with the flag.
     * Given Registry has no version with that number
     * When start-task runs with --force-start
     * Then the command exits 1 naming the FQN and the version
     */
    @Test
    void whenVersionNotInRegistryWithForceThenFails() {
        registry.versions = List.of();

        run(command(true));

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Version 1.2.0 of data product urn:dp:sales not found in Registry");
        assertThat(presenter.failedSteps).containsExactly(StartTaskStep.LOOK_UP_VERSION);
        assertThat(devOps.calls).doesNotContain("execute");
    }

    /**
     * Scenario: Blocked open activity without the flag prints the hint.
     * Given an open RUNNING activity whose requested task is RUNNING from an abandoned run
     * When start-task runs without --force-start
     * Then a hint to re-run with --force-start is logged
     * And DevOps' refusal is shown and the command exits 1
     */
    @Test
    void whenOpenActivityBlockedWithoutForceThenWarnsHintAndRequests() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1", activity("a1", RUNNING, task("build", RUNNING)));
        devOps.requestRefusal = new BadRequestException("Task build is not PENDING");

        run(command(false));

        assertThat(presenter.warnings).hasSize(1);
        assertThat(presenter.warnings.get(0)).contains("Open activity a1 cannot accept task build: build=RUNNING")
                .contains("re-run start-task with --force-start");
        assertThat(presenter.failure).isInstanceOf(BadRequestException.class).hasMessage("Task build is not PENDING");
        assertThat(presenter.failedSteps).containsExactly(StartTaskStep.REQUEST_TASK);
        assertThat(devOps.calls).noneMatch(call -> call.startsWith("status:") || call.startsWith("cancel:"));
    }

    /**
     * Scenario: Blocked open activity is replaced with the flag.
     * Given an open RUNNING activity whose requested task is RUNNING from an abandoned run
     * When start-task runs with --force-start
     * Then the running task is recorded CANCELED and the activity is waited until terminal
     * And a new activity is executed from Registry and the task starts
     */
    @Test
    void whenOpenActivityBlockedWithForceThenReplacesAndStartsNew() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1", activity("a1", RUNNING, task("build", RUNNING)), activity("a1", CANCELED, task("build", CANCELED)));
        devOps.creation = new ActivityCreation.Created(activity("a2", PENDING, task("build", PENDING)));
        devOps.read("a2",
                activity("a2", RUNNING, task("build", PENDING)),
                activity("a2", RUNNING, task("build", RUNNING)));

        run(command(true));

        assertThat(presenter.failure).isNull();
        assertThat(presenter.warnings).containsExactly("--force-start: replacing open activity a1");
        assertThat(devOps.calls).containsSubsequence("find-open", "get:a1", "status:build:CANCELED", "get:a1", "get:a1", "execute", "get:a2", "request:build:run-1");
        assertThat(devOps.calls).noneMatch(call -> call.startsWith("cancel:"));
        assertThat(presenter.startedActivity.uuid()).isEqualTo("a2");
    }

    /**
     * Scenario: Open activity without the requested task is not replaced.
     * Given an open RUNNING activity with no task of the requested name
     * When start-task runs with --force-start
     * Then the activity is not replaced and DevOps' "not found" refusal fails the command
     */
    @Test
    void whenOpenActivityLacksTaskWithForceThenDoesNotReplace() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1", activity("a1", RUNNING, task("other", RUNNING)));
        devOps.requestRefusal = new NotFoundException("Task build not found in activity dev");

        run(command(true));

        assertThat(presenter.failure).isInstanceOf(NotFoundException.class).hasMessage("Task build not found in activity dev");
        assertThat(presenter.warnings).isEmpty();
        assertThat(devOps.calls).noneMatch(call -> call.startsWith("status:") || call.startsWith("cancel:") || call.equals("execute"));
    }

    @Test
    void whenRegistryVersionHasNoContentThenFailsNamingTheVersion() {
        registry.content = null;

        run(command(false));

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class).hasMessage("Version 1.2.0 has no descriptor content");
        assertThat(devOps.calls).doesNotContain("execute");
    }

    @Test
    void whenStatusesAreNullThenTheWaitsKeepPollingUntilDevOpsDecides() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1",
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", null, task("build", PENDING)),
                activity("a1", RUNNING, task("build", null)),
                activity("a1", RUNNING, task("build", RUNNING)));

        run(command(false));

        assertThat(presenter.failure).isNull();
        assertThat(presenter.startedTask.status()).isEqualTo(RUNNING);
    }

    @Test
    void whenWaitingThenEachPollIsPresentedForDebugLogging() {
        devOps.creation = new ActivityCreation.Created(activity("a1", PENDING, task("build", PENDING)));
        devOps.read("a1",
                activity("a1", PENDING, task("build", PENDING)),
                activity("a1", PENDING, task("build", PENDING)),
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", RUNNING, task("build", PENDING)),
                activity("a1", RUNNING, task("build", RUNNING)));

        run(command(false));

        assertThat(presenter.polls).containsExactly(
                "AWAIT_ACTIVITY_APPROVAL: activity a1 is PENDING",
                "AWAIT_ACTIVITY_APPROVAL: activity a1 is PENDING",
                "AWAIT_TASK_APPROVAL: task build is PENDING");
    }

    @Test
    void whenForcedReplacementLeavesTheActivityOpenThenItIsCanceledAndAwaited() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1",
                activity("a1", RUNNING, task("build", RUNNING)),
                activity("a1", RUNNING, task("build", CANCELED)),
                activity("a1", CANCELED, task("build", CANCELED)));
        devOps.creation = new ActivityCreation.Created(activity("a2", PENDING, task("build", PENDING)));
        devOps.read("a2", activity("a2", RUNNING, task("build", PENDING)), activity("a2", RUNNING, task("build", RUNNING)));

        run(command(true));

        assertThat(presenter.failure).isNull();
        assertThat(devOps.calls).containsSubsequence("status:build:CANCELED", "cancel:a1", "get:a1", "execute");
    }

    @Test
    void whenForcedReplacementNeverClosesTheActivityThenFailsAtTheCloseTimeout() {
        devOps.openSearches.add(List.of(summary("a1")));
        devOps.read("a1", activity("a1", RUNNING, task("build", RUNNING)));
        StartTaskCommand command = new StartTaskCommand(FQN, VERSION, "dev", "build", "run-1", true,
                Duration.ofSeconds(10), Duration.ofSeconds(30), Duration.ofMinutes(10));

        run(command);

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Open activity a1 did not close within PT30S");
        assertThat(presenter.failedSteps).containsExactly(StartTaskStep.REPLACE_ACTIVITY);
        assertThat(devOps.calls).doesNotContain("execute");
    }
}

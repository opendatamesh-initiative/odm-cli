package org.opendatamesh.cli.usecases.devops.completetask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.FakeClock;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.FakeDevOps;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.FakeFiles;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.RecordingPresenter;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.instrumented;
import static org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.resultsOnly;

/**
 * Unit tests of the complete-task use case with in-memory ports.
 * Generated from BDMD-5443, prompt {@code spdd/prompt/BDMD-5443-202610291156-[Feat]-commands-devops-v2-start-complete-task.md}.
 */
class CompleteTaskTest {

    private FakeDevOps devOps;
    private FakeFiles files;
    private FakeClock clock;
    private RecordingPresenter presenter;

    @BeforeEach
    void setUp() {
        devOps = new FakeDevOps();
        files = new FakeFiles();
        clock = new FakeClock();
        presenter = new RecordingPresenter();
        files.files.put("build.log", "line one\nline two\n");
        files.files.put("results.json", "{\"coverage\": 80}");
    }

    private void run(CompleteTaskCommand command) {
        new CompleteTask(command, presenter, devOps, files, clock).execute();
    }

    /**
     * Scenario: Logs, results and status in order.
     * Given a RUNNING instrumented task with the run id
     * When the pipeline runs complete-task with --status SUCCEEDED, a logs file and a results file
     * Then logs, results and status are recorded in that order by run id
     * And the command exits 0
     */
    @Test
    void whenStatusWithLogsAndResultsThenRecordsInOrderByRunId() {
        run(instrumented(ExecutionStatus.SUCCEEDED, List.of("build.log"), "results.json"));

        assertThat(presenter.failure).isNull();
        assertThat(devOps.calls).containsExactly(
                "logs:run-1:[line one\nline two]",
                "results:run-1:{\"coverage\": 80}",
                "status:run-1:SUCCEEDED");
        assertThat(presenter.completedStatus).isEqualTo(ExecutionStatus.SUCCEEDED);
    }

    /**
     * Scenario: Results upload fails and the task is closed as FAILED.
     * Given DevOps refuses the results upload
     * When complete-task runs with --status SUCCEEDED
     * Then the status FAILED is recorded and the command exits 1
     */
    @Test
    void whenResultsUploadFailsThenRecordsFailedAndFails() {
        devOps.refuse("results", "Results are not valid");

        run(instrumented(ExecutionStatus.SUCCEEDED, List.of("build.log"), "results.json"));

        assertThat(devOps.calls).last().isEqualTo("status:run-1:FAILED");
        assertThat(presenter.completedStatus).isEqualTo(ExecutionStatus.FAILED);
        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Results upload failed, task run-1 was closed as FAILED: Results are not valid");
        assertThat(presenter.failedSteps).containsExactly(CompleteTaskStep.UPLOAD_RESULTS);
    }

    /**
     * Scenario: Only the logs upload fails and the requested status is kept.
     * Given DevOps refuses the logs upload
     * When complete-task runs with --status SUCCEEDED
     * Then the status SUCCEEDED is recorded and the command exits 1
     */
    @Test
    void whenOnlyLogsUploadFailsThenRecordsRequestedStatusAndFails() {
        devOps.refuse("logs", "Log content is too long");

        run(instrumented(ExecutionStatus.SUCCEEDED, List.of("build.log"), "results.json"));

        assertThat(devOps.calls).last().isEqualTo("status:run-1:SUCCEEDED");
        assertThat(devOps.calls).anyMatch(call -> call.startsWith("results:"));
        assertThat(presenter.completedStatus).isEqualTo(ExecutionStatus.SUCCEEDED);
        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Logs upload failed, task run-1 was closed as SUCCEEDED: Log content is too long");
        assertThat(presenter.failedSteps).containsExactly(CompleteTaskStep.UPLOAD_LOGS);
    }

    /**
     * Scenario: Invalid local input sends nothing.
     * Given the results file is not a JSON object
     * When complete-task runs
     * Then the command exits 1 naming the issue and no DevOps call is made
     */
    @Test
    void whenResultsNotJsonObjectThenFailsWithoutCalls() {
        files.files.put("results.json", "[1, 2]");

        run(instrumented(ExecutionStatus.SUCCEEDED, List.of("build.log"), "results.json"));

        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessage("Invalid input: results file results.json is not a JSON object");
        assertThat(presenter.failedSteps).containsExactly(CompleteTaskStep.CHECK_INPUT);
        assertThat(devOps.calls).isEmpty();
    }

    /**
     * Scenario: Results only.
     * Given a RUNNING full-control task whose provider run id is the pipeline's id
     * When the pipeline runs complete-task --results-only --provider-run-id and a results file
     * Then only the results are recorded and the command exits 0
     */
    @Test
    void whenResultsOnlyThenRecordsOnlyResults() {
        run(resultsOnly("results.json"));

        assertThat(presenter.failure).isNull();
        assertThat(devOps.calls).containsExactly("results:run-1:{\"coverage\": 80}");
        assertThat(presenter.resultsUploaded).isTrue();
    }

    /**
     * Scenario: Run id not stored yet.
     * Given DevOps reports no RUNNING task for the run id twice, then accepts
     * When complete-task uploads results
     * Then it retries and succeeds
     */
    @Test
    void whenRunIdNotFoundTwiceThenRetriesAndSucceeds() {
        devOps.notFoundBefore.put("results", 2);

        run(resultsOnly("results.json"));

        assertThat(presenter.failure).isNull();
        assertThat(devOps.calls).hasSize(3);
        assertThat(clock.sleeps).containsExactly(Duration.ofSeconds(2), Duration.ofSeconds(2));
        assertThat(presenter.resultsUploaded).isTrue();
    }

    /**
     * Scenario: Run id never found.
     * Given DevOps reports no RUNNING task for every attempt
     * When complete-task uploads results
     * Then the command exits 1 naming the run id and the child-pipeline hint
     */
    @Test
    void whenRunIdNeverFoundThenFailsNamingRunId() {
        devOps.notFoundBefore.put("results", 100);

        run(resultsOnly("results.json"));

        assertThat(devOps.calls).hasSize(5);
        assertThat(clock.sleeps).hasSize(4);
        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessageContaining("No RUNNING task with provider run id run-1 after 5 attempts")
                .hasMessageContaining("the parent's CI_PIPELINE_ID");
        assertThat(presenter.failedSteps).containsExactly(CompleteTaskStep.UPLOAD_RESULTS);
        assertThat(presenter.resultsUploaded).isFalse();
    }

    @Test
    void whenRunIdNeverFoundInInstrumentedModeThenLookupHappensOnceAndFailsAtTheFirstStep() {
        devOps.notFoundBefore.put("logs", 100);
        devOps.notFoundBefore.put("results", 100);
        devOps.notFoundBefore.put("status", 100);

        run(instrumented(ExecutionStatus.SUCCEEDED, List.of("build.log"), "results.json"));

        assertThat(devOps.calls).hasSize(5).allMatch(call -> call.startsWith("logs:"));
        assertThat(clock.sleeps).hasSize(4);
        assertThat(presenter.failedSteps).containsExactly(CompleteTaskStep.UPLOAD_LOGS);
        assertThat(presenter.failure).isInstanceOf(UseCaseException.class)
                .hasMessageContaining("No RUNNING task with provider run id run-1 after 5 attempts");
        assertThat(presenter.completedStatus).isNull();
    }
}

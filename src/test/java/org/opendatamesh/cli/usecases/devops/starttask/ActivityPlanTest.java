package org.opendatamesh.cli.usecases.devops.starttask;

import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.command;

/**
 * Tests of the activity plan, with the cases ported from the UI's {@code ExecuteDevOpsV2ActivityFlow.test.js} (BDMD-5518).
 * Generated from BDMD-5443, prompt {@code spdd/prompt/BDMD-5443-202610291156-[Feat]-commands-devops-v2-start-complete-task.md}.
 */
class ActivityPlanTest {

    private final ActivityPlan plan = new ActivityPlan();

    private static DescriptorTask task(String name, String templateName, Integer order, String executor) {
        return new DescriptorTask(name, templateName, templateName == null ? null : templateName + " description", executor, order, Map.of());
    }

    private static StartTaskCommand commandForTask(String taskName) {
        StartTaskCommand base = command(false);
        return new StartTaskCommand(base.dataProductFqn(), base.dataProductVersion(), base.activityName(), taskName,
                base.providerRunId(), false, base.pollInterval(), base.activityApprovalTimeout(), base.taskApprovalTimeout());
    }

    private static DescriptorLifecycle lifecycle(String stage, DescriptorTask... tasks) {
        LinkedHashMap<String, List<DescriptorTask>> stages = new LinkedHashMap<>();
        stages.put("build", List.of(task("x", null, null, "executor")));
        stages.put(stage, List.of(tasks));
        return new DescriptorLifecycle("1.0.0", stages);
    }

    /**
     * Scenario: Plan follows the UI mapping.
     * Given a DPDS 1.x descriptor whose stage lists tasks with order, names and service $href
     * When the activity is planned
     * Then tasks are ordered by order, named by name or template.name or task_<n>, with executor and params
     */
    @Test
    void whenStageHasTasksThenPlanFollowsUiMapping() {
        DescriptorTask second = new DescriptorTask("second", "ignored", "Second task", "azure-devops", 2, Map.of());
        DescriptorTask first = new DescriptorTask(null, "from-template", "First task", "github-actions", 1,
                Map.of("out", "${dev.results.task1.out}", "count", "2", "flag", "true"));
        DescriptorTask unnamed = new DescriptorTask("  ", null, null, "gitlab", null, Map.of());

        DevOpsActivity activity = plan.plan(lifecycle("dev", second, first, unnamed), "version-uuid", commandForTask("second"));

        assertThat(activity.uuid()).isNull();
        assertThat(activity.status()).isNull();
        assertThat(activity.dataProductVersionUuid()).isEqualTo("version-uuid");
        assertThat(activity.dataProductFqn()).isEqualTo("urn:dp:sales");
        assertThat(activity.dataProductVersionNumber()).isEqualTo("1.2.0");
        assertThat(activity.name()).isEqualTo("dev");
        assertThat(activity.sortOrder()).isEqualTo(1);
        assertThat(activity.tasks()).extracting("name").containsExactly("from-template", "second", "task_3");
        assertThat(activity.tasks().get(0).description()).isEqualTo("First task");
        assertThat(activity.tasks().get(0).executorName()).isEqualTo("github-actions");
        assertThat(activity.tasks().get(0).pipelineParameters())
                .containsEntry("out", "${dev.results.task1.out}").containsEntry("count", "2").containsEntry("flag", "true");
        assertThat(activity.tasks().get(0).status()).isNull();
        assertThat(activity.tasks().get(0).providerRunId()).isNull();
    }

    /**
     * Scenario: Unknown stage, empty stage, missing executor or duplicate name.
     * Given a descriptor with one of these defects
     * When the activity is planned
     * Then start-task exits 1 before Execute Activity with the matching message
     */
    @Test
    void whenStageUnknownEmptyMissingExecutorOrDuplicateThenFails() {
        assertThatThrownBy(() -> plan.plan(new DescriptorLifecycle("1.0.0", new LinkedHashMap<>(Map.of("prod", List.of()))), "v", command(false)))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Stage dev not found in lifecycleInfo; available: prod");
        assertThatThrownBy(() -> plan.plan(lifecycle("dev"), "v", command(false)))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Stage dev has no tasks in lifecycleInfo");
        assertThatThrownBy(() -> plan.plan(lifecycle("dev", task("build", null, null, " ")), "v", command(false)))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Task build: missing service $href (executor)");
        assertThatThrownBy(() -> plan.plan(lifecycle("dev", task("build", null, null, "e"), task("build", null, null, "e")), "v", command(false)))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Duplicate task name build in stage dev");
    }

    /**
     * Scenario: Requested task is not in the stage.
     * Given the stage's derived task names do not include --task
     * When the activity is planned
     * Then start-task exits 1 listing the stage's task names
     */
    @Test
    void whenTaskNotInStageThenFailsListingNames() {
        assertThatThrownBy(() -> plan.plan(lifecycle("dev", task("lint", null, null, "e"), task("test", null, null, "e")), "v", command(false)))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Task build not found in stage dev; tasks: lint, test");
    }

    @Test
    void whenDescriptorIsNotDpds1xThenFails() {
        DescriptorLifecycle lifecycle = new DescriptorLifecycle("2.0.0", new LinkedHashMap<>(Map.of("dev", List.of(task("build", null, null, "e")))));

        assertThatThrownBy(() -> plan.plan(lifecycle, "v", command(false)))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Descriptor version 2.0.0 is not supported; only DPDS 1.x");
    }

    @Test
    void whenTasksShareTheSameOrderThenDescriptorOrderIsKept() {
        DevOpsActivity activity = plan.plan(lifecycle("dev", task("build", null, 1, "e"), task("lint", null, 1, "e")), "v", command(false));

        assertThat(activity.tasks()).extracting("name").containsExactly("build", "lint");
    }
}

package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.INVALID;

/** The activity to execute for a stage of the descriptor, following the UI mapping (BDMD-5518). Pure: no ports. */
class ActivityPlan {

    DevOpsActivity plan(DescriptorLifecycle lifecycle, String versionUuid, StartTaskCommand command) {
        checkDescriptorVersion(lifecycle);
        List<DescriptorTask> stageTasks = stageTasks(lifecycle, command.activityName());
        List<Ranked> ordered = orderedTasks(stageTasks);
        List<DevOpsTask> tasks = ordered.stream().map(this::toTask).toList();
        checkUniqueNames(tasks, command.activityName());
        checkRequestedTask(tasks, command);
        return new DevOpsActivity(
                null,
                versionUuid,
                command.dataProductFqn(),
                command.dataProductVersion(),
                command.activityName(),
                new ArrayList<>(lifecycle.stages().keySet()).indexOf(command.activityName()),
                null,
                tasks
        );
    }

    private void checkDescriptorVersion(DescriptorLifecycle lifecycle) {
        String version = lifecycle.descriptorVersion();
        if (version == null || !version.startsWith("1.")) {
            throw unsupportedDescriptor(version);
        }
    }

    private List<DescriptorTask> stageTasks(DescriptorLifecycle lifecycle, String activityName) {
        List<DescriptorTask> tasks = lifecycle.stages().get(activityName);
        if (tasks == null) {
            throw unknownStage(activityName, lifecycle);
        }
        if (tasks.isEmpty()) {
            throw noTasks(activityName);
        }
        return tasks;
    }

    private List<Ranked> orderedTasks(List<DescriptorTask> stageTasks) {
        List<Ranked> ranked = new ArrayList<>();
        for (int index = 0; index < stageTasks.size(); index++) {
            DescriptorTask task = stageTasks.get(index);
            int position = index + 1;
            ranked.add(new Ranked(task, task.order() != null ? task.order() : position));
        }
        ranked.sort(Comparator.comparingInt(Ranked::rank));
        return ranked;
    }

    private DevOpsTask toTask(Ranked ranked) {
        DescriptorTask task = ranked.task();
        String name = taskName(ranked);
        if (isBlank(task.executorRef())) {
            throw missingExecutor(name);
        }
        return new DevOpsTask(name, task.templateDescription(), null, task.executorRef(), null, task.params());
    }

    private String taskName(Ranked ranked) {
        DescriptorTask task = ranked.task();
        if (!isBlank(task.name())) {
            return task.name();
        }
        if (!isBlank(task.templateName())) {
            return task.templateName();
        }
        return "task_" + ranked.rank();
    }

    private void checkUniqueNames(List<DevOpsTask> tasks, String activityName) {
        Set<String> seen = new HashSet<>();
        for (DevOpsTask task : tasks) {
            if (!seen.add(task.name())) {
                throw duplicateTaskName(task.name(), activityName);
            }
        }
    }

    private void checkRequestedTask(List<DevOpsTask> tasks, StartTaskCommand command) {
        if (tasks.stream().noneMatch(task -> task.name().equals(command.taskName()))) {
            throw unknownTask(command, tasks);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private UseCaseException unsupportedDescriptor(String version) {
        return new UseCaseException(INVALID, "Descriptor version " + version + " is not supported; only DPDS 1.x");
    }

    private UseCaseException unknownStage(String activityName, DescriptorLifecycle lifecycle) {
        return new UseCaseException(INVALID, "Stage " + activityName + " not found in lifecycleInfo; available: "
                + String.join(", ", lifecycle.stages().keySet()));
    }

    private UseCaseException noTasks(String activityName) {
        return new UseCaseException(INVALID, "Stage " + activityName + " has no tasks in lifecycleInfo");
    }

    private UseCaseException missingExecutor(String taskName) {
        return new UseCaseException(INVALID, "Task " + taskName + ": missing service $href (executor)");
    }

    private UseCaseException duplicateTaskName(String taskName, String activityName) {
        return new UseCaseException(INVALID, "Duplicate task name " + taskName + " in stage " + activityName);
    }

    private UseCaseException unknownTask(StartTaskCommand command, List<DevOpsTask> tasks) {
        List<String> names = tasks.stream().map(DevOpsTask::name).toList();
        return new UseCaseException(INVALID, "Task " + command.taskName() + " not found in stage "
                + command.activityName() + "; tasks: " + String.join(", ", names));
    }

    /** A descriptor task with its sort rank: its order, else its 1-based position. */
    private record Ranked(DescriptorTask task, int rank) {
    }
}

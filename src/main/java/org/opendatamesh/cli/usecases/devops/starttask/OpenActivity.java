package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivityKey;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.CONFLICT;

/** The open activity of the data product version and stage: finding it, and replacing it when --force-start says so. */
class OpenActivity {

    private final StartTaskDevOpsOutboundPort devOpsPort;
    private final StartTaskClockOutboundPort clockPort;
    private final StartTaskPresenter presenter;
    private final StartTaskCommand command;

    OpenActivity(StartTaskDevOpsOutboundPort devOpsPort, StartTaskClockOutboundPort clockPort, StartTaskPresenter presenter, StartTaskCommand command) {
        this.devOpsPort = devOpsPort;
        this.clockPort = clockPort;
        this.presenter = presenter;
        this.command = command;
    }

    Optional<DevOpsActivity> find(DevOpsActivityKey key) {
        List<DevOpsActivity> open = devOpsPort.findOpenActivities(key);
        if (open.isEmpty()) {
            return Optional.empty();
        }
        if (open.size() > 1) {
            throw moreThanOneOpen(key, open);
        }
        return Optional.of(devOpsPort.getActivity(open.get(0).uuid()));
    }

    /**
     * @param onReplace runs just before the activity is replaced, so the caller can name the step
     * @return the activity to use, or empty when it was replaced and a new one must be created
     */
    Optional<DevOpsActivity> prepare(DevOpsActivity activity, Runnable onReplace) {
        if (!blocked(activity)) {
            return Optional.of(activity);
        }
        if (!command.forceStart()) {
            presenter.presentWarning(forceStartHint(activity));
            return Optional.of(activity);
        }
        onReplace.run();
        replace(activity);
        return Optional.empty();
    }

    private void replace(DevOpsActivity activity) {
        DevOpsActivityKey key = key();
        presenter.presentWarning("--force-start: replacing open activity " + activity.uuid());
        for (DevOpsTask running : activity.runningTasks()) {
            devOpsPort.recordTaskStatus(key, running.name(), ExecutionStatus.CANCELED);
        }
        if (devOpsPort.getActivity(activity.uuid()).isOpen()) {
            devOpsPort.cancelActivity(activity.uuid());
        }
        awaitTerminal(activity.uuid());
    }

    private void awaitTerminal(String uuid) {
        Instant deadline = clockPort.now().plus(command.activityApprovalTimeout());
        while (!terminal(devOpsPort.getActivity(uuid))) {
            if (!clockPort.now().isBefore(deadline)) {
                throw closeTimedOut(uuid, command.activityApprovalTimeout());
            }
            presenter.presentProgress(StartTaskStep.REPLACE_ACTIVITY, "waiting for activity " + uuid + " to close");
            clockPort.sleep(command.pollInterval());
        }
    }

    private boolean terminal(DevOpsActivity activity) {
        return activity.status() != null && activity.status().isTerminal();
    }

    private DevOpsActivityKey key() {
        return new DevOpsActivityKey(command.dataProductFqn(), command.dataProductVersion(), command.activityName());
    }

    private boolean blocked(DevOpsActivity activity) {
        if (activity.status() != ExecutionStatus.RUNNING) {
            return false;
        }
        Optional<DevOpsTask> requested = activity.task(command.taskName());
        return requested.isPresent()
                && (requested.get().status() != ExecutionStatus.PENDING || anotherTaskRunning(activity));
    }

    private boolean anotherTaskRunning(DevOpsActivity activity) {
        return activity.runningTasks().stream().anyMatch(task -> !task.name().equals(command.taskName()));
    }

    private String forceStartHint(DevOpsActivity activity) {
        String statuses = activity.tasks().stream()
                .map(task -> task.name() + "=" + task.status())
                .collect(Collectors.joining(", "));
        return "Open activity " + activity.uuid() + " cannot accept task " + command.taskName() + ": " + statuses
                + ". If the previous run is abandoned, re-run start-task with --force-start.";
    }

    private UseCaseException moreThanOneOpen(DevOpsActivityKey key, List<DevOpsActivity> open) {
        String uuids = open.stream().map(DevOpsActivity::uuid).collect(Collectors.joining(", "));
        return new UseCaseException(CONFLICT, "More than one open activity " + key.activityName() + " for "
                + key.dataProductFqn() + " " + key.dataProductVersionNumber() + ": " + uuids);
    }

    private UseCaseException closeTimedOut(String uuid, Duration timeout) {
        return new UseCaseException(CONFLICT, "Open activity " + uuid + " did not close within " + timeout);
    }
}

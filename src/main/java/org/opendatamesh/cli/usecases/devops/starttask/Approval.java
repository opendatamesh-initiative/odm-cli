package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.time.Duration;
import java.time.Instant;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.CONFLICT;
import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.INVALID;
import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.NOT_FOUND;

/** The waits for the policy approval of the activity and of the task. Nothing is written to DevOps. */
class Approval {

    private static final Duration PROGRESS_EVERY = Duration.ofMinutes(1);

    private final StartTaskDevOpsOutboundPort devOpsPort;
    private final StartTaskClockOutboundPort clockPort;
    private final StartTaskPresenter presenter;
    private final Duration pollInterval;
    private Instant lastProgress;

    Approval(StartTaskDevOpsOutboundPort devOpsPort, StartTaskClockOutboundPort clockPort, StartTaskPresenter presenter, Duration pollInterval) {
        this.devOpsPort = devOpsPort;
        this.clockPort = clockPort;
        this.presenter = presenter;
        this.pollInterval = pollInterval;
    }

    DevOpsActivity awaitActivity(String uuid, Duration timeout) {
        Instant deadline = clockPort.now().plus(timeout);
        lastProgress = null;
        while (true) {
            DevOpsActivity activity = devOpsPort.getActivity(uuid);
            switch (statusOrPending(activity.status())) {
                case RUNNING:
                    return activity;
                case FAILED:
                    throw activityRejected(uuid);
                case CANCELED:
                case SUCCEEDED:
                    throw activityEnded(uuid, activity);
                default:
                    break;
            }
            if (!clockPort.now().isBefore(deadline)) {
                throw activityTimedOut(uuid, timeout, activity);
            }
            presenter.presentPoll(StartTaskStep.AWAIT_ACTIVITY_APPROVAL, "activity " + uuid + " is " + activity.status());
            progress(StartTaskStep.AWAIT_ACTIVITY_APPROVAL, "activity " + uuid + " is " + activity.status());
            clockPort.sleep(pollInterval);
        }
    }

    DevOpsTask awaitTask(String uuid, String taskName, Duration timeout) {
        Instant deadline = clockPort.now().plus(timeout);
        lastProgress = null;
        while (true) {
            DevOpsActivity activity = devOpsPort.getActivity(uuid);
            DevOpsTask task = activity.task(taskName).orElseThrow(() -> taskMissing(taskName, uuid));
            switch (statusOrPending(task.status())) {
                case RUNNING:
                    return task;
                case FAILED:
                    throw taskRejected(taskName);
                case CANCELED:
                    throw taskCanceled(taskName);
                case SUCCEEDED:
                    throw taskEnded(taskName, task, activity);
                default:
                    break;
            }
            if (activity.status() != null && activity.status().isTerminal()) {
                throw taskEnded(taskName, task, activity);
            }
            if (!clockPort.now().isBefore(deadline)) {
                throw taskTimedOut(taskName, timeout, task);
            }
            presenter.presentPoll(StartTaskStep.AWAIT_TASK_APPROVAL, "task " + taskName + " is " + task.status());
            progress(StartTaskStep.AWAIT_TASK_APPROVAL, "task " + taskName + " is " + task.status());
            clockPort.sleep(pollInterval);
        }
    }

    /** A status that DevOps did not send means the state is not decided yet: keep waiting. */
    private ExecutionStatus statusOrPending(ExecutionStatus status) {
        return status != null ? status : ExecutionStatus.PENDING;
    }

    private void progress(StartTaskStep step, String detail) {
        Instant now = clockPort.now();
        if (lastProgress == null || !now.isBefore(lastProgress.plus(PROGRESS_EVERY))) {
            lastProgress = now;
            presenter.presentProgress(step, "waiting: " + detail);
        }
    }

    private UseCaseException activityRejected(String uuid) {
        return new UseCaseException(INVALID, "Activity " + uuid + " was rejected (FAILED)");
    }

    private UseCaseException activityEnded(String uuid, DevOpsActivity activity) {
        return new UseCaseException(CONFLICT, "Activity " + uuid + " ended " + activity.status() + " before the task started");
    }

    private UseCaseException activityTimedOut(String uuid, Duration timeout, DevOpsActivity activity) {
        return new UseCaseException(CONFLICT, "Timed out after " + timeout + " waiting for activity " + uuid
                + " approval (still " + activity.status() + ")");
    }

    private UseCaseException taskMissing(String taskName, String uuid) {
        return new UseCaseException(NOT_FOUND, "Task " + taskName + " not found in activity " + uuid);
    }

    private UseCaseException taskRejected(String taskName) {
        return new UseCaseException(INVALID, "Task " + taskName + " was rejected (FAILED); the activity is now FAILED");
    }

    private UseCaseException taskCanceled(String taskName) {
        return new UseCaseException(CONFLICT, "Task " + taskName + " was canceled (CANCELED) before it started");
    }

    private UseCaseException taskEnded(String taskName, DevOpsTask task, DevOpsActivity activity) {
        return new UseCaseException(CONFLICT, "Task " + taskName + " ended before it started running: task "
                + task.status() + ", activity " + activity.status());
    }

    private UseCaseException taskTimedOut(String taskName, Duration timeout, DevOpsTask task) {
        return new UseCaseException(CONFLICT, "Timed out after " + timeout + " waiting for task " + taskName
                + " approval (still " + task.status() + ")");
    }
}

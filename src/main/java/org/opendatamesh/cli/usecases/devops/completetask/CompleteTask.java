package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.function.Supplier;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.INVALID;
import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.NOT_FOUND;

/**
 * Closes an instrumented task (logs, results, status) or, with results only, uploads the results of a
 * full-control task. Every write is addressed by provider run id.
 */
class CompleteTask implements UseCase {

    private final CompleteTaskCommand command;
    private final CompleteTaskPresenter presenter;
    private final CompleteTaskDevOpsOutboundPort devOpsPort;
    private final CompleteTaskClockOutboundPort clockPort;
    private final TaskReportCheck taskReportCheck;
    private CompleteTaskStep currentStep;
    private UseCaseException lookupFailure;
    private CompleteTaskStep lookupFailureStep;

    CompleteTask(
            CompleteTaskCommand command,
            CompleteTaskPresenter presenter,
            CompleteTaskDevOpsOutboundPort devOpsPort,
            CompleteTaskFilesOutboundPort filesPort,
            CompleteTaskClockOutboundPort clockPort
    ) {
        this.command = command;
        this.presenter = presenter;
        this.devOpsPort = devOpsPort;
        this.clockPort = clockPort;
        this.taskReportCheck = new TaskReportCheck(filesPort, presenter);
    }

    @Override
    public void execute() {
        try {
            TaskReport report = checkInput();
            if (command.resultsOnly()) {
                uploadResults(report);
                presenter.presentResultsUploaded(command.providerRunId());
            } else {
                reportAndClose(report);
            }
        } catch (Exception e) {
            presenter.presentFailedStep(currentStep);
            presenter.presentException(e);
        }
    }

    private TaskReport checkInput() {
        enter(CompleteTaskStep.CHECK_INPUT, "checking the input files");
        return taskReportCheck.check(command);
    }

    private void reportAndClose(TaskReport report) {
        RuntimeException logsFailure = attempt(() -> uploadLogs(report));
        RuntimeException resultsFailure = attempt(() -> uploadResults(report));
        failIfTaskNotFound();
        ExecutionStatus status = resultsFailure != null ? ExecutionStatus.FAILED : command.status();
        recordStatus(status);
        presenter.presentTaskCompleted(command.providerRunId(), status);
        failIfPartial(logsFailure, resultsFailure, status);
    }

    private void uploadLogs(TaskReport report) {
        enter(CompleteTaskStep.UPLOAD_LOGS, report.logs().isEmpty()
                ? "no logs to upload"
                : "uploading " + report.logs().size() + " log rows");
        if (!report.logs().isEmpty()) {
            untilTaskFound(() -> devOpsPort.recordLogs(command.providerRunId(), report.logs()));
        }
    }

    private void uploadResults(TaskReport report) {
        enter(CompleteTaskStep.UPLOAD_RESULTS, report.results() == null ? "no results to upload" : "uploading the results");
        if (report.results() != null) {
            untilTaskFound(() -> devOpsPort.recordResults(command.providerRunId(), report.results()));
        }
    }

    private void recordStatus(ExecutionStatus status) {
        enter(CompleteTaskStep.RECORD_STATUS, "recording status " + status);
        untilTaskFound(() -> devOpsPort.recordStatus(command.providerRunId(), status));
    }

    private void enter(CompleteTaskStep step, String detail) {
        currentStep = step;
        presenter.presentProgress(step, detail);
    }

    private void untilTaskFound(Supplier<TaskWrite> write) {
        if (lookupFailure != null) {
            throw lookupFailure;
        }
        int attempts = Math.max(1, command.runIdLookupAttempts());
        for (int attempt = 1; write.get() == TaskWrite.NO_RUNNING_TASK; attempt++) {
            if (attempt >= attempts) {
                lookupFailure = noRunningTask(attempts);
                lookupFailureStep = currentStep;
                throw lookupFailure;
            }
            clockPort.sleep(command.runIdLookupInterval());
        }
    }

    /** Once the run id lookup is exhausted no later write can succeed: the failure is reported at the step where it was found. */
    private void failIfTaskNotFound() {
        if (lookupFailure != null) {
            currentStep = lookupFailureStep;
            throw lookupFailure;
        }
    }

    private void failIfPartial(RuntimeException logsFailure, RuntimeException resultsFailure, ExecutionStatus status) {
        if (resultsFailure != null) {
            currentStep = CompleteTaskStep.UPLOAD_RESULTS;
            throw new UseCaseException(INVALID, "Results upload failed, task " + command.providerRunId()
                    + " was closed as FAILED: " + resultsFailure.getMessage(), resultsFailure);
        }
        if (logsFailure != null) {
            currentStep = CompleteTaskStep.UPLOAD_LOGS;
            throw new UseCaseException(INVALID, "Logs upload failed, task " + command.providerRunId()
                    + " was closed as " + status + ": " + logsFailure.getMessage(), logsFailure);
        }
    }

    private RuntimeException attempt(Runnable step) {
        try {
            step.run();
            return null;
        } catch (RuntimeException e) {
            if (e != lookupFailure) {
                presenter.presentProgress(currentStep, "failed, the task will still be closed: " + e.getMessage());
            }
            return e;
        }
    }

    private UseCaseException noRunningTask(int attempts) {
        return new UseCaseException(NOT_FOUND, "No RUNNING task with provider run id " + command.providerRunId()
                + " after " + attempts + " attempts. Check that the run id matches the one given to start-task, or, in a"
                + " child pipeline, the parent's CI_PIPELINE_ID.");
    }
}

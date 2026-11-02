package org.opendatamesh.cli.commands.platform.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.configs.platform.DevOpsCommandsConfiguration;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskCommand;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFactory;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskPresenter;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CompleteTaskCommandExecutor extends PicoCliCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(CompleteTaskCommandExecutor.class);

    private final CompleteTaskFactory completeTaskFactory;

    private String providerRunId;
    private ExecutionStatus status;
    private boolean resultsOnly;
    private List<String> logFiles = new ArrayList<>();
    private String resultsFile;

    protected CompleteTaskCommandExecutor(OdmCliConfiguration odmCliConfiguration, CompleteTaskFactory completeTaskFactory) {
        super(odmCliConfiguration);
        this.completeTaskFactory = completeTaskFactory;
    }

    @Override
    protected void executeUseCase() {
        DevOpsCommandsConfiguration timings = configuredTimings();
        CompleteTaskCommand command = new CompleteTaskCommand(
                providerRunId,
                status,
                resultsOnly,
                List.copyOf(logFiles),
                resultsFile,
                timings.getRunIdLookupAttempts(),
                timings.getRunIdLookupInterval()
        );
        completeTaskFactory.buildCompleteTask(command, new LoggingCompleteTaskPresenter()).execute();
    }

    private DevOpsCommandsConfiguration configuredTimings() {
        if (odmCliConfiguration.getPlatform() == null || odmCliConfiguration.getPlatform().getDevops() == null) {
            return new DevOpsCommandsConfiguration();
        }
        return odmCliConfiguration.getPlatform().getDevops();
    }

    void setProviderRunId(String providerRunId) {
        this.providerRunId = providerRunId;
    }

    void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    void setResultsOnly(boolean resultsOnly) {
        this.resultsOnly = resultsOnly;
    }

    List<String> getLogFiles() {
        return logFiles;
    }

    void setLogFiles(List<String> logFiles) {
        this.logFiles = logFiles != null ? logFiles : new ArrayList<>();
    }

    void setResultsFile(String resultsFile) {
        this.resultsFile = resultsFile;
    }

    private static class LoggingCompleteTaskPresenter implements CompleteTaskPresenter {

        @Override
        public void presentProgress(CompleteTaskStep step, String detail) {
            log.info("[{}] {}", step, detail);
        }

        @Override
        public void presentFailedStep(CompleteTaskStep step) {
            log.error("complete-task failed at step {}", step);
        }

        @Override
        public void presentTaskCompleted(String providerRunId, ExecutionStatus recordedStatus) {
            log.info("Task with provider run id {} was closed as {}", providerRunId, recordedStatus);
        }

        @Override
        public void presentResultsUploaded(String providerRunId) {
            log.info("Results of the task with provider run id {} were uploaded", providerRunId);
        }
    }
}

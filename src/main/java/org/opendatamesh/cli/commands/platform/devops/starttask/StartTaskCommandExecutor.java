package org.opendatamesh.cli.commands.platform.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.configs.platform.DevOpsCommandsConfiguration;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskCommand;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskFactory;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskPresenter;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class StartTaskCommandExecutor extends PicoCliCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(StartTaskCommandExecutor.class);

    private final StartTaskFactory startTaskFactory;

    private String dataProductFqn;
    private String dataProductVersion;
    private String activityName;
    private String taskName;
    private String providerRunId;
    private boolean forceStart;
    private Duration pollInterval;
    private Duration activityApprovalTimeout;
    private Duration taskApprovalTimeout;

    protected StartTaskCommandExecutor(OdmCliConfiguration odmCliConfiguration, StartTaskFactory startTaskFactory) {
        super(odmCliConfiguration);
        this.startTaskFactory = startTaskFactory;
    }

    @Override
    protected void executeUseCase() {
        DevOpsCommandsConfiguration timings = configuredTimings();
        StartTaskCommand command = new StartTaskCommand(
                dataProductFqn,
                dataProductVersion,
                activityName,
                taskName,
                providerRunId,
                forceStart,
                pollInterval != null ? pollInterval : timings.getPollInterval(),
                activityApprovalTimeout != null ? activityApprovalTimeout : timings.getActivityApprovalTimeout(),
                taskApprovalTimeout != null ? taskApprovalTimeout : timings.getTaskApprovalTimeout()
        );
        startTaskFactory.buildStartTask(command, new LoggingStartTaskPresenter()).execute();
    }

    private DevOpsCommandsConfiguration configuredTimings() {
        if (odmCliConfiguration.getPlatform() == null || odmCliConfiguration.getPlatform().getDevops() == null) {
            return new DevOpsCommandsConfiguration();
        }
        return odmCliConfiguration.getPlatform().getDevops();
    }

    void setDataProductFqn(String dataProductFqn) {
        this.dataProductFqn = dataProductFqn;
    }

    void setDataProductVersion(String dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    void setProviderRunId(String providerRunId) {
        this.providerRunId = providerRunId;
    }

    void setForceStart(boolean forceStart) {
        this.forceStart = forceStart;
    }

    void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    void setActivityApprovalTimeout(Duration activityApprovalTimeout) {
        this.activityApprovalTimeout = activityApprovalTimeout;
    }

    void setTaskApprovalTimeout(Duration taskApprovalTimeout) {
        this.taskApprovalTimeout = taskApprovalTimeout;
    }

    private static class LoggingStartTaskPresenter implements StartTaskPresenter {

        @Override
        public void presentProgress(StartTaskStep step, String detail) {
            log.info("[{}] {}", step, detail);
        }

        @Override
        public void presentPoll(StartTaskStep step, String detail) {
            log.debug("[{}] {}", step, detail);
        }

        @Override
        public void presentWarning(String message) {
            log.warn(message);
        }

        @Override
        public void presentFailedStep(StartTaskStep step) {
            log.error("start-task failed at step {}", step);
        }

        @Override
        public void presentTaskStarted(DevOpsActivity activity, DevOpsTask task) {
            log.info("Task {} of activity {} ({}) is RUNNING with provider run id {}",
                    task.name(), activity.name(), activity.uuid(), task.providerRunId());
        }
    }
}

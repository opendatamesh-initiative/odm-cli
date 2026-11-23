package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivityKey;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.clients.platform.devops.PlatformDevOpsClient;
import org.opendatamesh.cli.exceptions.BadRequestException;

import java.util.List;

class StartTaskDevOpsOutboundPortImpl implements StartTaskDevOpsOutboundPort {

    /**
     * The text of the DevOps refusal when an activity with the same natural key is already open. It is emitted
     * by {@code ExecuteActivity.java} (line 203) in {@code odm-platform-pp-devops-server}. If DevOps rewords it,
     * concurrent creation becomes a plain failure: keep this constant and the test in step with that line.
     */
    static final String ALREADY_OPEN_MESSAGE = "is already PENDING or RUNNING";

    private final PlatformDevOpsClient devOpsClient;

    StartTaskDevOpsOutboundPortImpl(PlatformDevOpsClient devOpsClient) {
        this.devOpsClient = devOpsClient;
    }

    @Override
    public List<DevOpsActivity> findOpenActivities(DevOpsActivityKey key) {
        return devOpsClient.findOpenActivities(key);
    }

    @Override
    public DevOpsActivity getActivity(String uuid) {
        return devOpsClient.getActivity(uuid);
    }

    @Override
    public ActivityCreation executeActivity(DevOpsActivity plan) {
        try {
            return new ActivityCreation.Created(devOpsClient.executeActivity(plan));
        } catch (BadRequestException e) {
            if (e.getMessage() != null && e.getMessage().contains(ALREADY_OPEN_MESSAGE)) {
                return new ActivityCreation.AlreadyOpen();
            }
            throw e;
        }
    }

    @Override
    public DevOpsActivity requestTaskExecution(DevOpsActivityKey key, String taskName, String providerRunId) {
        return devOpsClient.requestTaskExecution(key, taskName, providerRunId);
    }

    @Override
    public DevOpsActivity cancelActivity(String uuid) {
        return devOpsClient.cancelActivity(uuid);
    }

    @Override
    public void recordTaskStatus(DevOpsActivityKey key, String taskName, ExecutionStatus status) {
        devOpsClient.recordTaskStatus(key, taskName, status);
    }
}

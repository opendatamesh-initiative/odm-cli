package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.clients.platform.devops.PlatformDevOpsClient;
import org.opendatamesh.cli.exceptions.NotFoundException;

import java.util.List;

/** Writes by provider run id. DevOps answers 404 when no RUNNING task has the id: that is NO_RUNNING_TASK. */
class CompleteTaskDevOpsOutboundPortImpl implements CompleteTaskDevOpsOutboundPort {

    private final PlatformDevOpsClient devOpsClient;

    CompleteTaskDevOpsOutboundPortImpl(PlatformDevOpsClient devOpsClient) {
        this.devOpsClient = devOpsClient;
    }

    @Override
    public TaskWrite recordLogs(String providerRunId, List<String> logs) {
        return write(() -> devOpsClient.recordTaskLogsByRunId(providerRunId, logs));
    }

    @Override
    public TaskWrite recordResults(String providerRunId, String results) {
        return write(() -> devOpsClient.recordTaskResultsByRunId(providerRunId, results));
    }

    @Override
    public TaskWrite recordStatus(String providerRunId, ExecutionStatus status) {
        return write(() -> devOpsClient.recordTaskStatusByRunId(providerRunId, status));
    }

    private TaskWrite write(Runnable request) {
        try {
            request.run();
            return TaskWrite.RECORDED;
        } catch (NotFoundException e) {
            return TaskWrite.NO_RUNNING_TASK;
        }
    }
}

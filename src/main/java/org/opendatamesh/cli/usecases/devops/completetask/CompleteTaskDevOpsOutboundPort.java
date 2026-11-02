package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;

import java.util.List;

interface CompleteTaskDevOpsOutboundPort {
    TaskWrite recordLogs(String providerRunId, List<String> logs);

    TaskWrite recordResults(String providerRunId, String results);

    TaskWrite recordStatus(String providerRunId, ExecutionStatus status);
}

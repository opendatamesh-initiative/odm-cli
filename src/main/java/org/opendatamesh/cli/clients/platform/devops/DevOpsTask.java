package org.opendatamesh.cli.clients.platform.devops;

import java.util.Map;

public record DevOpsTask(
        String name,
        String description,
        ExecutionStatus status,
        String executorName,
        String providerRunId,
        Map<String, String> pipelineParameters
) {
}

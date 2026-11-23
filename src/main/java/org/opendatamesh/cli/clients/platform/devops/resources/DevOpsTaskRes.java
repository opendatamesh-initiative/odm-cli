package org.opendatamesh.cli.clients.platform.devops.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DevOpsTaskRes(
        String name,
        String description,
        String status,
        String providerRunId,
        String executorName,
        Map<String, String> pipelineParameters
) {
}

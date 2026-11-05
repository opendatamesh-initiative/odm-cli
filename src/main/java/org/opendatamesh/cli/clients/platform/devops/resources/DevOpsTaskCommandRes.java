package org.opendatamesh.cli.clients.platform.devops.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Body of the task commands (request execution, logs, results, status). Every field is nullable and only the
 * non-null ones are sent, so a run-id write carries no natural-key field.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DevOpsTaskCommandRes(
        String providerRunId,
        String dataProductFqn,
        String dataProductVersionNumber,
        String activityName,
        String taskName,
        String status,
        List<DevOpsTaskTextRes> logs,
        List<DevOpsTaskTextRes> results
) {
}

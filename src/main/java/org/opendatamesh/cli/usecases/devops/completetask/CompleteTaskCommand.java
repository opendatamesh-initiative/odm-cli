package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;

import java.time.Duration;
import java.util.List;

public record CompleteTaskCommand(
        String providerRunId,
        ExecutionStatus status,
        boolean resultsOnly,
        List<String> logFiles,
        String resultsFile,
        int runIdLookupAttempts,
        Duration runIdLookupInterval
) {
}

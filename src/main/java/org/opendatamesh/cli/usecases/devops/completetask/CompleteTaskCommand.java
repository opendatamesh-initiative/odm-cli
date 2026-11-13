package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;

import java.time.Duration;
import java.util.List;

/**
 * The input of complete-task. The status selects the mode: with a status the command closes an instrumented task,
 * without one it uploads only the results of a full-control task.
 */
public record CompleteTaskCommand(
        String providerRunId,
        ExecutionStatus status,
        List<String> logFiles,
        String resultsFile,
        int runIdLookupAttempts,
        Duration runIdLookupInterval
) {

    /** True for a full-control task: no status was given, so only the results are uploaded. */
    public boolean resultsOnly() {
        return status == null;
    }
}

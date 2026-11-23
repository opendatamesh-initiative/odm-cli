package org.opendatamesh.cli.usecases.devops.starttask;

import java.time.Duration;

public record StartTaskCommand(
        String dataProductFqn,
        String dataProductVersionNumber,
        String activityName,
        String taskName,
        String providerRunId,
        boolean forceStart,
        Duration pollInterval,
        Duration activityApprovalTimeout,
        Duration taskApprovalTimeout
) {
}

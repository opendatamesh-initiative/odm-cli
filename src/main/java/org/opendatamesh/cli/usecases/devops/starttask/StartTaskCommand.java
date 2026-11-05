package org.opendatamesh.cli.usecases.devops.starttask;

import java.time.Duration;

public record StartTaskCommand(
        String dataProductFqn,
        String dataProductVersion,
        String activityName,
        String taskName,
        String providerRunId,
        boolean forceStart,
        Duration pollInterval,
        Duration activityApprovalTimeout,
        Duration taskApprovalTimeout
) {
}

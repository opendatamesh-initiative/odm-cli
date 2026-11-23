package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivityKey;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;

import java.util.List;

interface StartTaskDevOpsOutboundPort {
    List<DevOpsActivity> findOpenActivities(DevOpsActivityKey key);

    DevOpsActivity getActivity(String uuid);

    ActivityCreation executeActivity(DevOpsActivity plan);

    DevOpsActivity requestTaskExecution(DevOpsActivityKey key, String taskName, String providerRunId);

    DevOpsActivity cancelActivity(String uuid);

    void recordTaskStatus(DevOpsActivityKey key, String taskName, ExecutionStatus status);
}

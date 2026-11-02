package org.opendatamesh.cli.clients.platform.devops;

import java.util.List;
import java.util.Optional;

public record DevOpsActivity(
        String uuid,
        String dataProductVersionUuid,
        String dataProductFqn,
        String dataProductVersionTag,
        String name,
        Integer sortOrder,
        ExecutionStatus status,
        List<DevOpsTask> tasks
) {

    public Optional<DevOpsTask> task(String name) {
        return tasks.stream().filter(task -> task.name().equals(name)).findFirst();
    }

    public List<DevOpsTask> runningTasks() {
        return tasks.stream().filter(task -> task.status() == ExecutionStatus.RUNNING).toList();
    }

    /** An open activity is one that is still PENDING or RUNNING. */
    public boolean isOpen() {
        return status == ExecutionStatus.PENDING || status == ExecutionStatus.RUNNING;
    }
}

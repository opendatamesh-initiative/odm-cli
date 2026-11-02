package org.opendatamesh.cli.clients.platform.devops;

import java.util.List;

/**
 * Client of the ODM Platform DevOps service, V2 API. Base path: {@code {url}/api/v2/pp/devops/activities}.
 */
public interface PlatformDevOpsClient {

    /**
     * {@code GET ?dataProductFqn=&dataProductVersionTag=&name=&statuses=PENDING,RUNNING&page=0&size=2&sort=}.
     *
     * @return at most 2 open activities, without tasks
     */
    List<DevOpsActivity> findOpenActivities(DevOpsActivityKey key);

    /** {@code GET /{uuid}}: the activity with its tasks. */
    DevOpsActivity getActivity(String uuid);

    /** {@code POST /execute}: creates the activity from the plan. Sends no secret headers. */
    DevOpsActivity executeActivity(DevOpsActivity plan);

    /** {@code POST /tasks/request-execution}. */
    DevOpsActivity requestTaskExecution(DevOpsActivityKey key, String taskName, String providerRunId);

    /** {@code POST /cancel}. */
    DevOpsActivity cancelActivity(String uuid);

    /** {@code POST /tasks/status}, addressed by natural key and task name. */
    void recordTaskStatus(DevOpsActivityKey key, String taskName, ExecutionStatus status);

    /** {@code POST /tasks/logs}, addressed by provider run id. */
    void recordTaskLogsByRunId(String providerRunId, List<String> logs);

    /** {@code POST /tasks/results}, addressed by provider run id. */
    void recordTaskResultsByRunId(String providerRunId, String results);

    /** {@code POST /tasks/status}, addressed by provider run id. */
    void recordTaskStatusByRunId(String providerRunId, ExecutionStatus status);
}

package org.opendatamesh.cli.clients.platform.devops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsActivityEnvelopeRes;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsActivityRes;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsActivitySearchOptions;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsErrorRes;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsTaskCommandRes;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsTaskRes;
import org.opendatamesh.cli.clients.platform.devops.resources.DevOpsTaskTextRes;
import org.opendatamesh.cli.configs.platform.DevOpsCommandsConfiguration;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.BadRequestException;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.NotFoundException;
import org.opendatamesh.cli.exceptions.ResourceConflictException;
import org.opendatamesh.cli.exceptions.client.ClientException;
import org.opendatamesh.cli.exceptions.client.ClientResourceMappingException;
import org.opendatamesh.cli.utils.client.RestUtils;
import org.opendatamesh.cli.utils.client.RestUtilsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

class PlatformDevOpsClientImpl implements PlatformDevOpsClient {

    static final String ACTIVITIES_ROUTE = "/api/v2/pp/devops/activities";
    private static final String OPEN_STATUSES = "PENDING,RUNNING";
    private static final ObjectMapper ERROR_MAPPER = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(PlatformDevOpsClientImpl.class);

    private final RestUtils restUtils;
    private final String activitiesUrl;
    private final int conflictRetries;
    private final Duration conflictRetryInterval;

    PlatformDevOpsClientImpl(
            RestTemplate restTemplate,
            PlatformServiceCredentials devOpsService,
            DevOpsCommandsConfiguration commands
    ) {
        this.restUtils = RestUtilsFactory.getAuthenticatedRestUtils(
                restTemplate,
                devOpsService.getHeaders(),
                devOpsService.getOauth2()
        );
        this.activitiesUrl = devOpsService.getUrl() + ACTIVITIES_ROUTE;
        this.conflictRetries = commands.getConflictRetries();
        this.conflictRetryInterval = commands.getConflictRetryInterval();
    }

    @Override
    public List<DevOpsActivity> findOpenActivities(DevOpsActivityKey key) {
        log.debug("DevOps: find open activities {} {} {}", key.dataProductFqn(), key.dataProductVersionNumber(), key.activityName());
        return call("find open activities", () -> restUtils.getPage(
                        activitiesUrl,
                        null,
                        Pageable.ofSize(2),
                        new DevOpsActivitySearchOptions(key.dataProductFqn(), key.dataProductVersionNumber(), key.activityName(), OPEN_STATUSES),
                        DevOpsActivityRes.class
                ).getContent().stream()
                .map(this::toActivity)
                .toList());
    }

    @Override
    public DevOpsActivity getActivity(String uuid) {
        log.debug("DevOps: get activity {}", uuid);
        return call("get activity", () -> toActivity(restUtils.get(activitiesUrl + "/{id}", null, uuid, DevOpsActivityRes.class)));
    }

    @Override
    public DevOpsActivity executeActivity(DevOpsActivity plan) {
        log.debug("DevOps: execute activity {} {} {}", plan.dataProductFqn(), plan.dataProductVersionNumber(), plan.name());
        return call("execute activity", () -> toActivity(restUtils.genericPost(
                activitiesUrl + "/execute",
                null,
                new DevOpsActivityEnvelopeRes(toPlanRes(plan)),
                DevOpsActivityEnvelopeRes.class
        ).activity()));
    }

    @Override
    public DevOpsActivity requestTaskExecution(DevOpsActivityKey key, String taskName, String providerRunId) {
        log.debug("DevOps: request execution of task {} of activity {} with run id {}", taskName, key.activityName(), providerRunId);
        DevOpsTaskCommandRes body = new DevOpsTaskCommandRes(providerRunId, key.dataProductFqn(), key.dataProductVersionNumber(),
                key.activityName(), taskName, null, null, null);
        return call("request task execution", () -> toActivity(restUtils.genericPost(
                activitiesUrl + "/tasks/request-execution",
                null,
                body,
                DevOpsActivityEnvelopeRes.class
        ).activity()));
    }

    @Override
    public DevOpsActivity cancelActivity(String uuid) {
        log.debug("DevOps: cancel activity {}", uuid);
        DevOpsActivityRes activity = new DevOpsActivityRes(uuid, null, null, null, null, null, null, null);
        return call("cancel activity", () -> toActivity(restUtils.genericPost(
                activitiesUrl + "/cancel",
                null,
                new DevOpsActivityEnvelopeRes(activity),
                DevOpsActivityEnvelopeRes.class
        ).activity()));
    }

    @Override
    public void recordTaskStatus(DevOpsActivityKey key, String taskName, ExecutionStatus status) {
        log.debug("DevOps: record status {} of task {} of activity {}", status, taskName, key.activityName());
        DevOpsTaskCommandRes body = new DevOpsTaskCommandRes(null, key.dataProductFqn(), key.dataProductVersionNumber(),
                key.activityName(), taskName, status.name(), null, null);
        post("record task status", "/tasks/status", body);
    }

    @Override
    public void recordTaskLogsByRunId(String providerRunId, List<String> logs) {
        log.debug("DevOps: record {} log rows of the task with run id {}", logs.size(), providerRunId);
        DevOpsTaskCommandRes body = new DevOpsTaskCommandRes(providerRunId, null, null, null, null, null,
                logs.stream().map(DevOpsTaskTextRes::new).toList(), null);
        post("record task logs", "/tasks/logs", body);
    }

    @Override
    public void recordTaskResultsByRunId(String providerRunId, String results) {
        log.debug("DevOps: record results of the task with run id {}", providerRunId);
        DevOpsTaskCommandRes body = new DevOpsTaskCommandRes(providerRunId, null, null, null, null, null,
                null, List.of(new DevOpsTaskTextRes(results)));
        post("record task results", "/tasks/results", body);
    }

    @Override
    public void recordTaskStatusByRunId(String providerRunId, ExecutionStatus status) {
        log.debug("DevOps: record status {} of the task with run id {}", status, providerRunId);
        DevOpsTaskCommandRes body = new DevOpsTaskCommandRes(providerRunId, null, null, null, null, status.name(), null, null);
        post("record task status", "/tasks/status", body);
    }

    private void post(String operation, String path, DevOpsTaskCommandRes body) {
        call(operation, () -> restUtils.genericPost(activitiesUrl + path, null, body, JsonNode.class));
    }

    private <T> T call(String operation, Supplier<T> request) {
        int conflicts = 0;
        while (true) {
            try {
                return request.get();
            } catch (ClientException e) {
                if (e.getCode() == 409 && conflicts < conflictRetries) {
                    conflicts++;
                    log.debug("DevOps: {} hit a conflict, retry {} of {}", operation, conflicts, conflictRetries);
                    pauseBeforeRetry();
                } else {
                    throw translate(e);
                }
            } catch (ClientResourceMappingException e) {
                throw translate(e);
            }
        }
    }

    private void pauseBeforeRetry() {
        try {
            Thread.sleep(conflictRetryInterval.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while retrying an ODM Platform devops service request");
        }
    }

    private DevOpsActivityRes toPlanRes(DevOpsActivity plan) {
        List<DevOpsTaskRes> tasks = plan.tasks().stream()
                .map(task -> new DevOpsTaskRes(task.name(), task.description(), null, null, task.executorName(), task.pipelineParameters()))
                .toList();
        return new DevOpsActivityRes(null, plan.dataProductVersionUuid(), plan.dataProductFqn(), plan.dataProductVersionNumber(),
                plan.name(), plan.sortOrder(), null, tasks);
    }

    private DevOpsActivity toActivity(DevOpsActivityRes res) {
        List<DevOpsTask> tasks = res.tasks() == null ? List.of() : res.tasks().stream().map(this::toTask).toList();
        return new DevOpsActivity(res.uuid(), res.dataProductVersionUuid(), res.dataProductFqn(), res.dataProductVersionNumber(),
                res.name(), res.sortOrder(), toStatus(res.status()), tasks);
    }

    private DevOpsTask toTask(DevOpsTaskRes res) {
        return new DevOpsTask(res.name(), res.description(), toStatus(res.status()), res.executorName(),
                res.providerRunId(), res.pipelineParameters());
    }

    private ExecutionStatus toStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return ExecutionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new InternalException("ODM Platform devops service returned an unknown status: " + status);
        }
    }

    private RuntimeException translate(ClientException e) {
        String serverMessage = serverMessage(e.getResponseBody());
        if (e.getCode() == 409) {
            return new ResourceConflictException(serverMessage != null ? serverMessage
                    : "ODM Platform devops service reported a conflict that persisted after retrying");
        }
        if (serverMessage != null) {
            switch (e.getCode()) {
                case 400:
                    return new BadRequestException(serverMessage);
                case 404:
                    return new NotFoundException(serverMessage);
                default:
                    break;
            }
        }
        if (serverMessage == null && isTransportFailure(e)) {
            return new InternalException("Could not reach the ODM Platform devops service: " + e.getResponseBody().trim());
        }
        String detail = serverMessage != null ? serverMessage : describeUnreadableBody(e.getResponseBody());
        return new InternalException("ODM Platform devops service failed (HTTP " + e.getCode() + "): " + detail);
    }

    private RuntimeException translate(ClientResourceMappingException e) {
        return new InternalException("Unable to read the ODM Platform devops service response: " + e.getMessage());
    }

    private String serverMessage(String responseBody) {
        try {
            DevOpsErrorRes error = ERROR_MAPPER.readValue(responseBody, DevOpsErrorRes.class);
            return error.message() != null && !error.message().isBlank() ? error.message() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** The template wraps a transport failure as code 500 with the "I/O error ..." message of the RestClientException. */
    private boolean isTransportFailure(ClientException e) {
        return e.getCode() == 500 && e.getResponseBody() != null && e.getResponseBody().trim().startsWith("I/O error");
    }

    /** A body that is not the platform error JSON is described, never copied: it may be a page from a proxy. */
    private String describeUnreadableBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "no details";
        }
        String trimmed = responseBody.trim();
        String kind = trimmed.startsWith("<") ? "HTML or XML" : "text";
        return "unexpected non-JSON response (" + kind + ", " + trimmed.length() + " characters)";
    }
}

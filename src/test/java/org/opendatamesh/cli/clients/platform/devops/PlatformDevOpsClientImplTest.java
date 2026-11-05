package org.opendatamesh.cli.clients.platform.devops;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.configs.platform.DevOpsCommandsConfiguration;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.BadRequestException;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.NotFoundException;
import org.opendatamesh.cli.exceptions.ResourceConflictException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests of the DevOps client at the transport seam. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}.
 */
class PlatformDevOpsClientImplTest {

    private static final String BASE = "http://devops.test/api/v2/pp/devops/activities";
    private static final DevOpsActivityKey KEY = new DevOpsActivityKey("urn:dp:sales", "1.2.0", "dev");

    private MockRestServiceServer server;
    private PlatformDevOpsClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        PlatformServiceCredentials credentials = new PlatformServiceCredentials();
        credentials.setUrl("http://devops.test");
        DevOpsCommandsConfiguration commands = new DevOpsCommandsConfiguration();
        commands.setConflictRetries(2);
        commands.setConflictRetryInterval(Duration.ofMillis(1));
        client = new PlatformDevOpsClientImpl(restTemplate, credentials, commands);
    }

    private static final String ACTIVITY = """
            {"activity":{"uuid":"a1","dataProductVersionUuid":"v1","dataProductFqn":"urn:dp:sales","dataProductVersionNumber":"1.2.0",
             "name":"dev","sortOrder":0,"status":"PENDING","unknownField":1,
             "tasks":[{"name":"build","status":"PENDING","executorName":"gh","providerRunId":null,"pipelineParameters":{"k":"v"}}]}}
            """;

    @Test
    void whenFindingOpenActivitiesThenSearchesByNaturalKeyAndOpenStatusesWithPageSizeTwo() {
        server.expect(requestTo(BASE + "?dataProductFqn=urn:dp:sales&dataProductVersionNumber=1.2.0&name=dev&statuses=PENDING,RUNNING&page=0&size=2&sort="))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"content":[{"uuid":"a1","name":"dev","status":"RUNNING"}],"number":0,"size":2,"totalElements":1}
                        """, MediaType.APPLICATION_JSON));

        List<DevOpsActivity> activities = client.findOpenActivities(KEY);

        server.verify();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).uuid()).isEqualTo("a1");
        assertThat(activities.get(0).status()).isEqualTo(ExecutionStatus.RUNNING);
        assertThat(activities.get(0).tasks()).isEmpty();
        assertThat(activities.get(0).isOpen()).isTrue();
    }

    @Test
    void whenGettingActivityThenMapsTheTasks() {
        server.expect(requestTo(BASE + "/a1")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(ACTIVITY.replace("{\"activity\":", "").trim().replaceAll("}\\s*$", ""), MediaType.APPLICATION_JSON));

        DevOpsActivity activity = client.getActivity("a1");

        server.verify();
        assertThat(activity.tasks()).hasSize(1);
        assertThat(activity.task("build")).get().extracting(DevOpsTask::executorName).isEqualTo("gh");
        assertThat(activity.task("build").get().pipelineParameters()).containsEntry("k", "v");
        assertThat(activity.runningTasks()).isEmpty();
    }

    @Test
    void whenExecutingThenPostsThePlanWithoutStatusesOrSecrets() {
        server.expect(requestTo(BASE + "/execute")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"activity":{"dataProductVersionUuid":"v1","dataProductFqn":"urn:dp:sales","dataProductVersionNumber":"1.2.0","name":"dev","sortOrder":0,
                         "tasks":[{"name":"build","description":"Build","executorName":"gh","pipelineParameters":{"k":"v"}}]}}
                        """, true))
                .andRespond(withStatus(org.springframework.http.HttpStatus.CREATED).body(ACTIVITY).contentType(MediaType.APPLICATION_JSON));
        DevOpsTask task = new DevOpsTask("build", "Build", null, "gh", null, Map.of("k", "v"));

        DevOpsActivity created = client.executeActivity(new DevOpsActivity(null, "v1", "urn:dp:sales", "1.2.0", "dev", 0, null, List.of(task)));

        server.verify();
        assertThat(created.uuid()).isEqualTo("a1");
    }

    @Test
    void whenRequestingTaskExecutionThenPostsTheKeyTheTaskAndTheRunId() {
        server.expect(requestTo(BASE + "/tasks/request-execution")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"dataProductFqn":"urn:dp:sales","dataProductVersionNumber":"1.2.0","activityName":"dev","taskName":"build","providerRunId":"run-1"}
                        """, true))
                .andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));

        client.requestTaskExecution(KEY, "build", "run-1");

        server.verify();
    }

    @Test
    void whenCancelingThenPostsTheUuidOnly() {
        server.expect(requestTo(BASE + "/cancel")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"activity\":{\"uuid\":\"a1\"}}", true))
                .andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));

        client.cancelActivity("a1");

        server.verify();
    }

    @Test
    void whenRecordingStatusByKeyThenPostsTheKeyTheTaskAndTheStatus() {
        server.expect(requestTo(BASE + "/tasks/status")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"dataProductFqn":"urn:dp:sales","dataProductVersionNumber":"1.2.0","activityName":"dev","taskName":"build","status":"CANCELED"}
                        """, true))
                .andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));

        client.recordTaskStatus(KEY, "build", ExecutionStatus.CANCELED);

        server.verify();
    }

    @Test
    void whenRecordingByRunIdThenBodiesCarryTheRunIdAndNoAddressFields() {
        server.expect(requestTo(BASE + "/tasks/logs")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"run-1\",\"logs\":[{\"content\":\"a\"},{\"content\":\"b\"}]}", true))
                .andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/tasks/results")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"run-1\",\"results\":[{\"content\":\"{}\"}]}", true))
                .andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/tasks/status")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"run-1\",\"status\":\"SUCCEEDED\"}", true))
                .andExpect(jsonPath("$.taskName").doesNotExist())
                .andExpect(jsonPath("$.dataProductFqn").doesNotExist())
                .andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));

        client.recordTaskLogsByRunId("run-1", List.of("a", "b"));
        client.recordTaskResultsByRunId("run-1", "{}");
        client.recordTaskStatusByRunId("run-1", ExecutionStatus.SUCCEEDED);

        server.verify();
    }

    @Test
    void whenDevOpsRefusesWith400ThenTheServerMessageIsKeptVerbatim() {
        server.expect(requestTo(BASE + "/tasks/request-execution")).andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON).body("""
                        {"status":400,"error":"Bad Request","message":"Task build is not the next task. Task lint is still PENDING","path":"/x"}
                        """));

        assertThatThrownBy(() -> client.requestTaskExecution(KEY, "build", "run-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Task build is not the next task. Task lint is still PENDING");
    }

    @Test
    void whenDevOpsAnswers404ThenNotFoundCarriesTheServerMessage() {
        server.expect(requestTo(BASE + "/tasks/status")).andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON).body("""
                        {"status":404,"error":"Not Found","message":"No RUNNING task with provider run id run-1","path":"/x"}
                        """));

        assertThatThrownBy(() -> client.recordTaskStatusByRunId("run-1", ExecutionStatus.FAILED))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No RUNNING task with provider run id run-1");
    }

    @Test
    void whenDevOpsAnswers409ThenTheCallIsRetriedAndSucceeds() {
        server.expect(requestTo(BASE + "/cancel")).andRespond(withStatus(org.springframework.http.HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"serialization conflict\"}"));
        server.expect(requestTo(BASE + "/cancel")).andRespond(withSuccess(ACTIVITY, MediaType.APPLICATION_JSON));

        assertThat(client.cancelActivity("a1").uuid()).isEqualTo("a1");

        server.verify();
    }

    @Test
    void whenDevOpsKeepsAnswering409ThenConflictIsRaisedAfterTheRetries() {
        for (int i = 0; i < 3; i++) {
            server.expect(requestTo(BASE + "/cancel")).andRespond(withStatus(org.springframework.http.HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"serialization conflict\"}"));
        }

        assertThatThrownBy(() -> client.cancelActivity("a1"))
                .isInstanceOf(ResourceConflictException.class).hasMessage("serialization conflict");

        server.verify();
    }

    @Test
    void whenDevOpsFailsWith500ThenInternalExceptionNamesTheServiceAndStatus() {
        server.expect(requestTo(BASE + "/a1")).andRespond(withStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body("boom"));

        assertThatThrownBy(() -> client.getActivity("a1"))
                .isInstanceOf(InternalException.class)
                .hasMessage("ODM Platform devops service failed (HTTP 500): unexpected non-JSON response (text, 4 characters)")
                .hasNoCause();
    }

    @Test
    void whenTheBodyIsNotThePlatformErrorThenTheRawClientErrorIsNeverShown() {
        server.expect(requestTo(BASE + "/a1")).andRespond(withStatus(org.springframework.http.HttpStatus.BAD_GATEWAY).body("<html>bad gateway</html>"));

        assertThatThrownBy(() -> client.getActivity("a1"))
                .isInstanceOf(InternalException.class)
                .hasMessage("ODM Platform devops service failed (HTTP 502): unexpected non-JSON response (HTML or XML, 24 characters)")
                .hasMessageNotContaining("bad gateway");
    }

    @Test
    void whenTheTransportFailsThenInternalExceptionIsRaised() {
        server.expect(requestTo(BASE + "/a1")).andRespond(request -> {
            throw new java.io.IOException("connection refused");
        });

        assertThatThrownBy(() -> client.getActivity("a1"))
                .isInstanceOf(InternalException.class)
                .hasMessageStartingWith("Could not reach the ODM Platform devops service: I/O error");
    }
}

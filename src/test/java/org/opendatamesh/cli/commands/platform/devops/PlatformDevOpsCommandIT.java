package org.opendatamesh.cli.commands.platform.devops;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.opendatamesh.cli.OdmCliApplication;
import org.opendatamesh.cli.commands.OdmCliCommandIT;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.configs.platform.DevOpsCommandsConfiguration;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.configs.platform.PlatformServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Runs the {@code odm-cli platform devops} commands in the shared application context, with the DevOps
 * and Registry services replaced by a {@link MockRestServiceServer} on the application's {@link RestTemplate}.
 * Generated from BDMD-5443, prompt {@code spdd/prompt/BDMD-5443-202610291156-[Feat]-commands-devops-v2-start-complete-task.md}.
 */
@ExtendWith(OutputCaptureExtension.class)
public class PlatformDevOpsCommandIT extends OdmCliCommandIT {

    private static final String DEVOPS = "http://devops.test/api/v2/pp/devops/activities";
    private static final String SEARCH = DEVOPS
            + "?dataProductFqn=urn:dp:sales&dataProductVersionNumber=1.2.0&name=dev&statuses=PENDING,RUNNING&page=0&size=2&sort=";
    private static final String BUILD_PENDING = activity("RUNNING", "PENDING");
    private static final String BUILD_RUNNING = activity("RUNNING", "RUNNING");

    @Autowired
    private OdmCliApplication application;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OdmCliConfiguration configuration;

    @TempDir
    Path directory;

    private MockRestServiceServer server;
    private ClientHttpRequestFactory originalRequestFactory;
    private PlatformServices originalServices;
    private DevOpsCommandsConfiguration originalTimings;

    @BeforeEach
    void prepareDevOpsServices() {
        originalServices = configuration.getPlatform().getServices();
        originalTimings = configuration.getPlatform().getDevops();

        // The service URLs are not packaged, so the test sets its own
        PlatformServiceCredentials devops = new PlatformServiceCredentials();
        devops.setUrl("http://devops.test");
        PlatformServiceCredentials registry = new PlatformServiceCredentials();
        registry.setUrl("http://registry.test");
        PlatformServices services = new PlatformServices();
        services.setDevops(devops);
        services.setRegistry(registry);
        configuration.getPlatform().setServices(services);
        DevOpsCommandsConfiguration timings = new DevOpsCommandsConfiguration();
        timings.setPollInterval(Duration.ofMillis(10));
        timings.setRunIdLookupInterval(Duration.ofMillis(10));
        timings.setConflictRetryInterval(Duration.ofMillis(10));
        configuration.getPlatform().setDevops(timings);

        originalRequestFactory = restTemplate.getRequestFactory();
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @AfterEach
    void restoreServices() {
        restTemplate.setRequestFactory(originalRequestFactory);
        configuration.getPlatform().setServices(originalServices);
        configuration.getPlatform().setDevops(originalTimings);
    }

    private static String activity(String activityStatus, String taskStatus) {
        return "{\"uuid\":\"a1\",\"name\":\"dev\",\"status\":\"" + activityStatus
                + "\",\"tasks\":[{\"name\":\"build\",\"status\":\"" + taskStatus + "\"}]}";
    }

    private void expectGet(String url, String body) {
        server.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private void expectOpenActivityFound() {
        expectGet(SEARCH, "{\"content\":[{\"uuid\":\"a1\",\"status\":\"RUNNING\"}],\"number\":0,\"size\":2,\"totalElements\":1}");
        expectGet(DEVOPS + "/a1", BUILD_PENDING);
        expectGet(DEVOPS + "/a1", BUILD_PENDING);
    }

    private int startTask(String... extra) {
        String[] base = {"--interactive=false", "platform", "devops", "start-task",
                "--data-product-fqn", "urn:dp:sales", "--data-product-version", "1.2.0",
                "--activity", "dev", "--task", "build", "--provider-run-id", "run-1"};
        String[] args = new String[base.length + extra.length];
        System.arraycopy(base, 0, args, 0, base.length);
        System.arraycopy(extra, 0, args, base.length, extra.length);
        application.run(args);
        return application.getExitCode();
    }

    private int run(String... args) {
        application.run(args);
        return application.getExitCode();
    }

    @Test
    void whenOpenActivityThenStartTaskExitsZero(CapturedOutput output) {
        expectOpenActivityFound();
        server.expect(requestTo(DEVOPS + "/tasks/request-execution")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"dataProductFqn":"urn:dp:sales","dataProductVersionNumber":"1.2.0","activityName":"dev","taskName":"build","providerRunId":"run-1"}
                        """, true))
                .andRespond(withSuccess("{\"activity\":" + BUILD_PENDING + "}", MediaType.APPLICATION_JSON));
        expectGet(DEVOPS + "/a1", BUILD_RUNNING);

        assertThat(startTask()).isZero();

        server.verify();
        assertThat(output).contains("Task build of activity dev (a1) is RUNNING");
    }

    /**
     * Scenario: DevOps refuses the task request and the message is shown verbatim.
     * Given DevOps answers the task request with 400 "Task build is not the next task. Task lint is still PENDING"
     * When start-task requests the task
     * Then the command exits 1 and the log contains that message verbatim
     */
    @Test
    void whenTaskRequestRefusedThenExitsOneWithServerMessage(CapturedOutput output) {
        expectOpenActivityFound();
        server.expect(requestTo(DEVOPS + "/tasks/request-execution"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"status":400,"error":"Bad Request","message":"Task build is not the next task. Task lint is still PENDING","path":"/x"}
                        """));

        assertThat(startTask()).isEqualTo(1);

        server.verify();
        assertThat(output).contains("Task build is not the next task. Task lint is still PENDING")
                .contains("start-task failed at step REQUEST_TASK")
                .doesNotContain("responseBody");
    }

    @Test
    void whenDevOpsServiceIsNotConfiguredThenStartTaskExitsOneNamingTheKey(CapturedOutput output) {
        configuration.getPlatform().getServices().setDevops(null);

        assertThat(startTask()).isEqualTo(1);

        assertThat(output).contains("cli.platform.services.devops.url");
    }

    @Test
    void whenStatusWithLogsAndResultsThenCompleteTaskExitsZero() throws Exception {
        Path log = Files.writeString(directory.resolve("build.log"), "built\n");
        Path tests = Files.writeString(directory.resolve("tests.log"), "tested\n");
        Path results = Files.writeString(directory.resolve("results.json"), "{\"coverage\":80}");
        server.expect(requestTo(DEVOPS + "/tasks/logs")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"run-1\",\"logs\":[{\"content\":\"built\"},{\"content\":\"tested\"}]}", true))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(DEVOPS + "/tasks/results")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"run-1\",\"results\":[{\"content\":\"{\\\"coverage\\\":80}\"}]}", true))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(DEVOPS + "/tasks/status")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"run-1\",\"status\":\"SUCCEEDED\"}", true))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        int exitCode = run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "run-1",
                "--status", "succeeded", "--logs-file", log.toString(), "--logs-file", tests.toString(), "--results-file", results.toString());

        assertThat(exitCode).isZero();
        server.verify();
    }

    @Test
    void whenRunIdNeverFoundThenCompleteTaskExitsOneNamingTheRunId(CapturedOutput output) {
        for (int attempt = 0; attempt < 5; attempt++) {
            server.expect(requestTo(DEVOPS + "/tasks/status"))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                            .body("{\"status\":404,\"message\":\"No RUNNING task with provider run id run-1\"}"));
        }

        int exitCode = run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "run-1", "--status", "FAILED");

        assertThat(exitCode).isEqualTo(1);
        server.verify();
        assertThat(output).contains("No RUNNING task with provider run id run-1 after 5 attempts");
    }

    /**
     * Scenario: Results only.
     * Given a RUNNING full-control task whose provider run id is the pipeline's id
     * When the pipeline runs complete-task --results-only --provider-run-id and a results file
     * Then only the results are recorded and the command exits 0
     */
    @Test
    void whenResultsOnlyThenPostsResultsByRunIdAndExitsZero() throws Exception {
        Path results = Files.writeString(directory.resolve("results.json"), "{\"coverage\":80}");
        server.expect(requestTo(DEVOPS + "/tasks/results")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"providerRunId\":\"4242\",\"results\":[{\"content\":\"{\\\"coverage\\\":80}\"}]}", true))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        int exitCode = run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "4242",
                "--results-only", "--results-file", results.toString());

        assertThat(exitCode).isZero();
        server.verify();
    }

    /**
     * Scenario: Unresolved run id.
     * Given the run id option is the literal "$CI_PIPELINE_ID"
     * When complete-task runs
     * Then the command exits 2 before any call
     */
    @Test
    void whenRunIdUnresolvedThenExitsTwo() {
        int exitCode = run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "$CI_PIPELINE_ID", "--status", "SUCCEEDED");

        assertThat(exitCode).isEqualTo(2);
        server.verify();
    }

    /**
     * Scenario: Status and results-only together.
     * When complete-task runs with both --status and --results-only
     * Then the command exits 2
     */
    @Test
    void whenStatusAndResultsOnlyThenExitsTwo() {
        int exitCode = run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "run-1",
                "--status", "SUCCEEDED", "--results-only");

        assertThat(exitCode).isEqualTo(2);
        server.verify();
    }

    @Test
    void whenNeitherStatusNorResultsOnlyThenExitsTwo() {
        assertThat(run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "run-1")).isEqualTo(2);
    }

    @Test
    void whenStatusIsNotFinalThenExitsTwo() {
        assertThat(run("--interactive=false", "platform", "devops", "complete-task", "--provider-run-id", "run-1", "--status", "RUNNING")).isEqualTo(2);
    }

    /**
     * Scenario: Missing coordinate.
     * When start-task runs without --activity
     * Then the command exits 2
     */
    @Test
    void whenActivityMissingThenExitsTwo() {
        int exitCode = run("--interactive=false", "platform", "devops", "start-task", "--data-product-fqn", "urn:dp:sales",
                "--data-product-version", "1.2.0", "--task", "build", "--provider-run-id", "run-1");

        assertThat(exitCode).isEqualTo(2);
        server.verify();
    }

    @Test
    void whenDurationIsNotPositiveThenStartTaskExitsTwo() {
        assertThat(startTask("--poll-interval", "0s")).isEqualTo(2);
        assertThat(startTask("--task-timeout", "soon")).isEqualTo(2);
    }
}

package org.opendatamesh.cli.usecases.devops.starttask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivityKey;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProduct;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersion;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersionSummary;
import org.opendatamesh.cli.clients.platform.registry.RegistryValidationState;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory ports, presenter and builders shared by the start-task unit tests. */
final class StartTaskFakes {

    static final String FQN = "urn:dp:sales";
    static final String VERSION = "1.2.0";

    private StartTaskFakes() {
    }

    static DevOpsTask task(String name, ExecutionStatus status) {
        return new DevOpsTask(name, null, status, "executor", null, Map.of());
    }

    static DevOpsActivity activity(String uuid, ExecutionStatus status, DevOpsTask... tasks) {
        return new DevOpsActivity(uuid, "version-uuid", FQN, VERSION, "dev", 0, status, List.of(tasks));
    }

    static StartTaskCommand command(boolean forceStart) {
        return new StartTaskCommand(FQN, VERSION, "dev", "build", "run-1", forceStart,
                Duration.ofSeconds(5), Duration.ofMinutes(30), Duration.ofMinutes(10));
    }

    static JsonNode descriptor() {
        try {
            return new ObjectMapper().readTree("""
                    {"dataProductDescriptor":"1.0.0","info":{"version":"1.2.0"},
                     "internalComponents":{"lifecycleInfo":{
                       "dev":[{"name":"build","service":{"$href":"github-actions"},"template":{"description":"Build"}},
                              {"name":"lint","service":{"$href":"github-actions"}}],
                       "prod":[]}}}
                    """);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static class FakeClock implements StartTaskClockOutboundPort {
        private Instant now = Instant.parse("2026-10-29T10:00:00Z");

        @Override
        public Instant now() {
            return now;
        }

        @Override
        public void sleep(Duration duration) {
            now = now.plus(duration);
        }
    }

    static class FakeDevOps implements StartTaskDevOpsOutboundPort {
        final List<String> calls = new ArrayList<>();
        final Deque<List<DevOpsActivity>> openSearches = new ArrayDeque<>();
        final Map<String, Deque<DevOpsActivity>> reads = new HashMap<>();
        ActivityCreation creation;
        DevOpsActivity executedPlan;
        RuntimeException requestRefusal;

        void read(String uuid, DevOpsActivity... successiveStates) {
            reads.put(uuid, new ArrayDeque<>(List.of(successiveStates)));
        }

        @Override
        public List<DevOpsActivity> findOpenActivities(DevOpsActivityKey key) {
            calls.add("find-open");
            return openSearches.size() > 1 ? openSearches.poll() : openSearches.isEmpty() ? List.of() : openSearches.peek();
        }

        @Override
        public DevOpsActivity getActivity(String uuid) {
            calls.add("get:" + uuid);
            Deque<DevOpsActivity> states = reads.get(uuid);
            return states.size() > 1 ? states.poll() : states.peek();
        }

        @Override
        public ActivityCreation executeActivity(DevOpsActivity plan) {
            calls.add("execute");
            executedPlan = plan;
            return creation;
        }

        @Override
        public DevOpsActivity requestTaskExecution(DevOpsActivityKey key, String taskName, String providerRunId) {
            calls.add("request:" + taskName + ":" + providerRunId);
            if (requestRefusal != null) {
                throw requestRefusal;
            }
            return null;
        }

        @Override
        public DevOpsActivity cancelActivity(String uuid) {
            calls.add("cancel:" + uuid);
            return null;
        }

        @Override
        public void recordTaskStatus(DevOpsActivityKey key, String taskName, ExecutionStatus status) {
            calls.add("status:" + taskName + ":" + status);
        }
    }

    static class FakeRegistry implements StartTaskRegistryOutboundPort {
        int calls;
        List<RegistryDataProduct> products = List.of(new RegistryDataProduct("product-uuid", FQN));
        List<RegistryDataProductVersionSummary> versions = List.of(new RegistryDataProductVersionSummary("version-uuid", VERSION));
        RegistryValidationState state = RegistryValidationState.APPROVED;
        JsonNode content = descriptor();

        @Override
        public List<RegistryDataProduct> findDataProductsByFqn(String fqn) {
            calls++;
            return products;
        }

        @Override
        public List<RegistryDataProductVersionSummary> findDataProductVersions(String dataProductUuid, String versionNumber) {
            calls++;
            return versions;
        }

        @Override
        public RegistryDataProductVersion getDataProductVersion(String uuid) {
            calls++;
            return new RegistryDataProductVersion(uuid, VERSION, state, content);
        }
    }

    static class RecordingPresenter implements StartTaskPresenter {
        final List<String> progress = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> polls = new ArrayList<>();
        final List<StartTaskStep> failedSteps = new ArrayList<>();
        DevOpsTask startedTask;
        DevOpsActivity startedActivity;
        Exception failure;

        @Override
        public void presentProgress(StartTaskStep step, String detail) {
            progress.add(step + ": " + detail);
        }

        @Override
        public void presentPoll(StartTaskStep step, String detail) {
            polls.add(step + ": " + detail);
        }

        @Override
        public void presentWarning(String message) {
            warnings.add(message);
        }

        @Override
        public void presentFailedStep(StartTaskStep step) {
            failedSteps.add(step);
        }

        @Override
        public void presentTaskStarted(DevOpsActivity activity, DevOpsTask task) {
            startedActivity = activity;
            startedTask = task;
        }

        @Override
        public void presentException(Exception exception) {
            failure = exception;
        }
    }
}

package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.exceptions.BadRequestException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory ports and presenter shared by the complete-task unit tests. */
final class CompleteTaskFakes {

    private CompleteTaskFakes() {
    }

    static CompleteTaskCommand instrumented(ExecutionStatus status, List<String> logFiles, String resultsFile) {
        return new CompleteTaskCommand("run-1", status, false, logFiles, resultsFile, 5, Duration.ofSeconds(2));
    }

    static CompleteTaskCommand resultsOnly(String resultsFile) {
        return new CompleteTaskCommand("run-1", null, true, List.of(), resultsFile, 5, Duration.ofSeconds(2));
    }

    static class FakeDevOps implements CompleteTaskDevOpsOutboundPort {
        final List<String> calls = new ArrayList<>();
        final Map<String, Integer> notFoundBefore = new HashMap<>();
        final Map<String, RuntimeException> refusals = new HashMap<>();
        private final Map<String, Integer> attempts = new HashMap<>();

        private TaskWrite write(String kind, String callDescription) {
            calls.add(callDescription);
            if (refusals.containsKey(kind)) {
                throw refusals.get(kind);
            }
            int attempt = attempts.merge(kind, 1, Integer::sum);
            return attempt <= notFoundBefore.getOrDefault(kind, 0) ? TaskWrite.NO_RUNNING_TASK : TaskWrite.RECORDED;
        }

        void refuse(String kind, String message) {
            refusals.put(kind, new BadRequestException(message));
        }

        @Override
        public TaskWrite recordLogs(String providerRunId, List<String> logs) {
            return write("logs", "logs:" + providerRunId + ":" + logs);
        }

        @Override
        public TaskWrite recordResults(String providerRunId, String results) {
            return write("results", "results:" + providerRunId + ":" + results);
        }

        @Override
        public TaskWrite recordStatus(String providerRunId, ExecutionStatus status) {
            return write("status", "status:" + providerRunId + ":" + status);
        }
    }

    static class FakeFiles implements CompleteTaskFilesOutboundPort {
        final Map<String, String> files = new HashMap<>();

        @Override
        public ReportFile readLog(String path) {
            String content = files.get(path);
            return new ReportFile(path, content == null ? null : content.trim(), content != null);
        }

        @Override
        public ResultsFile readResults(String path) {
            String content = files.get(path);
            if (content == null) {
                return new ResultsFile(path, null, false, false);
            }
            return new ResultsFile(path, content.trim(), true, content.trim().startsWith("{"));
        }
    }

    static class FakeClock implements CompleteTaskClockOutboundPort {
        final List<Duration> sleeps = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            sleeps.add(duration);
        }
    }

    static class RecordingPresenter implements CompleteTaskPresenter {
        final List<String> progress = new ArrayList<>();
        final List<CompleteTaskStep> failedSteps = new ArrayList<>();
        ExecutionStatus completedStatus;
        boolean resultsUploaded;
        Exception failure;

        @Override
        public void presentProgress(CompleteTaskStep step, String detail) {
            progress.add(step + ": " + detail);
        }

        @Override
        public void presentFailedStep(CompleteTaskStep step) {
            failedSteps.add(step);
        }

        @Override
        public void presentTaskCompleted(String providerRunId, ExecutionStatus recordedStatus) {
            completedStatus = recordedStatus;
        }

        @Override
        public void presentResultsUploaded(String providerRunId) {
            resultsUploaded = true;
        }

        @Override
        public void presentException(Exception exception) {
            failure = exception;
        }
    }
}

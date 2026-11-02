package org.opendatamesh.cli.usecases.devops.completetask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.FakeFiles;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.RecordingPresenter;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFakes.instrumented;

/** Tests of each input rule of complete-task. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class TaskReportCheckTest {

    private FakeFiles files;
    private RecordingPresenter presenter;
    private TaskReportCheck check;

    @BeforeEach
    void setUp() {
        files = new FakeFiles();
        presenter = new RecordingPresenter();
        check = new TaskReportCheck(files, presenter);
        files.files.put("a.log", "alpha\n");
        files.files.put("blank.log", "  \n");
        files.files.put("results.json", "{\"k\":1}");
    }

    @Test
    void whenInputIsValidThenReportHasOneRowPerLogAndTheResults() {
        TaskReport report = check.check(instrumented(ExecutionStatus.FAILED, List.of("a.log"), "results.json"));

        assertThat(report.logs()).containsExactly("alpha");
        assertThat(report.results()).isEqualTo("{\"k\":1}");
    }

    @Test
    void whenNoFilesThenReportIsEmpty() {
        TaskReport report = check.check(instrumented(ExecutionStatus.FAILED, List.of(), null));

        assertThat(report.logs()).isEmpty();
        assertThat(report.results()).isNull();
    }

    @Test
    void whenLogsWithResultsOnlyThenFails() {
        CompleteTaskCommand command = new CompleteTaskCommand("run-1", null, true, List.of("a.log"), "results.json", 5, Duration.ofSeconds(1));

        assertThatThrownBy(() -> check.check(command)).isInstanceOf(UseCaseException.class)
                .hasMessage("Invalid input: --logs-file cannot be used with --results-only: logs are refused for full-control tasks");
    }

    @Test
    void whenResultsOnlyWithoutResultsFileThenFails() {
        CompleteTaskCommand command = new CompleteTaskCommand("run-1", null, true, List.of(), null, 5, Duration.ofSeconds(1));

        assertThatThrownBy(() -> check.check(command)).isInstanceOf(UseCaseException.class)
                .hasMessage("Invalid input: --results-only requires --results-file");
    }

    @Test
    void whenFilesAreUnreadableThenAllIssuesAreReportedTogether() {
        assertThatThrownBy(() -> check.check(instrumented(ExecutionStatus.SUCCEEDED, List.of("missing.log"), "missing.json")))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Invalid input: log file missing.log is missing or not readable; results file missing.json is missing or not readable");
    }

    @Test
    void whenResultsAreNotAJsonObjectThenFails() {
        files.files.put("results.json", "[]");

        assertThatThrownBy(() -> check.check(instrumented(ExecutionStatus.SUCCEEDED, List.of(), "results.json")))
                .isInstanceOf(UseCaseException.class)
                .hasMessage("Invalid input: results file results.json is not a JSON object");
    }

    @Test
    void whenLogIsBlankThenItIsSkippedWithANote() {
        TaskReport report = check.check(instrumented(ExecutionStatus.SUCCEEDED, List.of("blank.log", "a.log"), null));

        assertThat(report.logs()).containsExactly("alpha");
        assertThat(presenter.progress).containsExactly("CHECK_INPUT: log file blank.log is blank, skipping it");
    }
}

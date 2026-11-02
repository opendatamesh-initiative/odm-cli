package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.ArrayList;
import java.util.List;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.INVALID;

/** Checks every local input of complete-task before any DevOps call, and reads the files into a TaskReport. */
class TaskReportCheck {

    private final CompleteTaskFilesOutboundPort filesPort;
    private final CompleteTaskPresenter presenter;

    TaskReportCheck(CompleteTaskFilesOutboundPort filesPort, CompleteTaskPresenter presenter) {
        this.filesPort = filesPort;
        this.presenter = presenter;
    }

    TaskReport check(CompleteTaskCommand command) {
        List<String> issues = new ArrayList<>();
        logsWithResultsOnly(command, issues);
        resultsOnlyWithoutResults(command, issues);
        List<String> logs = command.resultsOnly() ? List.of() : readLogs(command, issues);
        String results = readResults(command, issues);
        if (!issues.isEmpty()) {
            throw new UseCaseException(INVALID, "Invalid input: " + String.join("; ", issues));
        }
        return new TaskReport(logs, results);
    }

    private void logsWithResultsOnly(CompleteTaskCommand command, List<String> issues) {
        if (command.resultsOnly() && !command.logFiles().isEmpty()) {
            issues.add("--logs-file cannot be used with --results-only: logs are refused for full-control tasks");
        }
    }

    private void resultsOnlyWithoutResults(CompleteTaskCommand command, List<String> issues) {
        if (command.resultsOnly() && command.resultsFile() == null) {
            issues.add("--results-only requires --results-file");
        }
    }

    private List<String> readLogs(CompleteTaskCommand command, List<String> issues) {
        List<String> logs = new ArrayList<>();
        for (String path : command.logFiles()) {
            ReportFile file = filesPort.readLog(path);
            if (!file.readable()) {
                issues.add(unreadableFile("log file", path));
            } else if (emptyLog(file)) {
                presenter.presentProgress(CompleteTaskStep.CHECK_INPUT, "log file " + path + " is blank, skipping it");
            } else {
                logs.add(file.content());
            }
        }
        return logs;
    }

    private String readResults(CompleteTaskCommand command, List<String> issues) {
        if (command.resultsFile() == null) {
            return null;
        }
        ResultsFile file = filesPort.readResults(command.resultsFile());
        if (!file.readable()) {
            issues.add(unreadableFile("results file", command.resultsFile()));
            return null;
        }
        if (!file.jsonObject()) {
            issues.add(resultsNotJsonObject(command.resultsFile()));
            return null;
        }
        return file.content();
    }

    private boolean emptyLog(ReportFile file) {
        return file.content() == null || file.content().isBlank();
    }

    private String unreadableFile(String kind, String path) {
        return kind + " " + path + " is missing or not readable";
    }

    private String resultsNotJsonObject(String path) {
        return "results file " + path + " is not a JSON object";
    }
}

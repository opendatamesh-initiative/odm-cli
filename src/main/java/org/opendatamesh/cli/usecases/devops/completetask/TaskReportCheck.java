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
        logsWithoutStatus(command, issues);
        resultsMissingWithoutStatus(command, issues);
        List<String> logs = command.resultsOnly() ? List.of() : readLogs(command, issues);
        String results = readResults(command, issues);
        if (!issues.isEmpty()) {
            throw new UseCaseException(INVALID, "Invalid input: " + String.join("; ", issues));
        }
        return new TaskReport(logs, results);
    }

    private void logsWithoutStatus(CompleteTaskCommand command, List<String> issues) {
        if (command.resultsOnly() && !command.logFiles().isEmpty()) {
            issues.add("--logs-file needs --status: logs are recorded only for instrumented tasks");
        }
    }

    private void resultsMissingWithoutStatus(CompleteTaskCommand command, List<String> issues) {
        if (command.resultsOnly() && command.resultsFile() == null) {
            issues.add("--results-file is required without --status: a full-control task uploads only its results");
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

# Unit completetask

Closes an instrumented task, or uploads the results of a full-control task. Every write is addressed by the provider run id alone. Entry class: `CompleteTask`, built by `CompleteTaskFactory` from a `CompleteTaskCommand` and a `CompleteTaskPresenter`.

## Input and output

- **In:** the run id, the final status, the results-only flag, the log files, the results file, and the run id lookup attempts and interval.
- **Out:** `presentTaskCompleted` with the recorded status, or `presentResultsUploaded` for results only. Any failure goes to `presentFailedStep` with a `CompleteTaskStep`, then to the presenter's exception handling.

## Procedure

Check the input. Then either upload the results (results only), or upload the logs, upload the results and record the status. The index README has the canonical script.

## Cases handled

- **Invalid local input:** every issue is collected and reported at once, before any DevOps call.
- **Results upload fails:** the status FAILED is recorded, and the command fails.
- **Logs upload fails:** the requested status is recorded, and the command fails.
- **The status is always attempted** in instrumented mode once the input is valid.
- **Run id not stored yet:** DevOps answers that no RUNNING task has the id. The write is retried at the configured interval, then fails naming the run id and hinting at the parent pipeline's id.

## Step class and its rules

- `TaskReportCheck` checks the input and reads the files into a `TaskReport`. Rules: `logsWithResultsOnly`, `resultsOnlyWithoutResults`, `unreadableFile`, `resultsNotJsonObject`, `emptyLog`.
- The rules of the entry class, at the bottom of `CompleteTask`: `untilTaskFound`, `failIfPartial`, `attempt`.

## Ports

`CompleteTaskDevOpsOutboundPort`, `CompleteTaskFilesOutboundPort` and `CompleteTaskClockOutboundPort`, with their `*Impl` classes. Types owned by the unit: `TaskReport`, `TaskWrite`, `ReportFile` and `ResultsFile`.

## Tests that prove it

- `CompleteTaskTest`: one test per scenario of the story.
- `TaskReportCheckTest`: each input rule.
- `CompleteTaskDevOpsOutboundPortImplTest` and `CompleteTaskFilesOutboundPortImplTest`: the ports.
- `PlatformDevOpsCommandIT`: the command end to end.

# odmcli platform devops

## Usage

`odm-cli platform devops`

## Description

Command to run tasks of the ODM Platform DevOps Service from a pipeline. A pipeline runs an instrumented task under
platform governance with two commands: `start-task` brings the task to `RUNNING`, and `complete-task` closes it by
uploading its logs and results and recording its final status. A full-control pipeline, which reports its own status,
uses `complete-task --results-only` to upload its results.

**Note:** To function correctly, the CLI must be configured with the `devops` service (see
[odmcli platform](cmd-platform.md)), and with the `registry` service when `start-task` has to create the activity. The DevOps
service must support addressing a task by its run id (see [DevOps prerequisite](#devops-prerequisite)).

## Commands

| Command                                                           | Description                                                                                                  |
|-------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| [`odmcli platform devops start-task`](cmd-platform-devops-start-task.md)       | Brings one instrumented task to `RUNNING`: finds or creates the activity, waits for the approvals, requests the task. |
| [`odmcli platform devops complete-task`](cmd-platform-devops-complete-task.md) | Closes a task (logs, results, status), or uploads the results of a full-control task.                       |

## Options

| Option          | Description                                |
|-----------------|--------------------------------------------|
| `--version, -V` | Prints the current version of the command. |

## Exit codes

| Code | Meaning                                                                                                                                  |
|------|------------------------------------------------------------------------------------------------------------------------------------------|
| `0`  | The command succeeded. `start-task` exits `0` only when both the activity and the task are `RUNNING`.                                    |
| `1`  | The command failed. The last log lines name the step that failed and the cause. DevOps refusals are shown verbatim.                      |
| `2`  | Invalid usage: a required option is missing, an option is empty or still an unresolved variable (`${...}`, `$NAME`, `$(...)`), a status or duration is invalid, or `--status` and `--results-only` are used together or both missing. |

The commands never prompt, whatever the `--interactive` setting, and the CLI keeps no state between commands and reads no
CI environment variable: the pipeline passes every value as an option.

## How the commands find the task

The CLI refers to a task by its name: `--task` must be the name of a task of the stage, derived from the descriptor. The name is, in order, the task `name`, the task template `name`, or `task_<n>`, where `<n>` is the task `order`,
or its position in the stage. Task names are unique within a stage. A step of the pipeline is paired with a task of the
stage by writing that name in the `start-task` line.

## Open activities

DevOps allows one open activity (`PENDING` or `RUNNING`) per data product version and activity name. `start-task` reuses an
open activity, and creates one only when none is open. The cases:

| Open activity found | Outcome without `--force-start` | With `--force-start` |
|---|---|---|
| `PENDING` (awaiting approval, for example a concurrent run) | `start-task` waits for the approval, then requests the task. | Same. |
| `RUNNING`, the requested task is the next `PENDING` one | The task is requested and started. | Same. |
| `RUNNING`, the requested task already `SUCCEEDED` | DevOps refuses the request, and a hint to use `--force-start` is logged. | The activity is replaced. |
| `RUNNING`, the requested task `RUNNING` (the previous run died) | Same refusal and hint. | The running task is recorded `CANCELED`, the activity ends, and a new one is created. |
| `RUNNING`, another task `RUNNING` or the task not next | DevOps refuses the request. | The activity is replaced when the blocker is a stale `RUNNING` task. |
| `RUNNING`, no task with the requested name (built from another descriptor or by the UI) | DevOps refuses the request. | Not replaced: the refusal stands. |
| Terminal | Not an open activity: a new one is created. | Same. |

An activity that was abandoned can also be unblocked with `complete-task --provider-run-id <the dead run's id> --status CANCELED`.

## DevOps prerequisite

`complete-task` addresses the task by `providerRunId` alone. This needs a DevOps service whose Record Task Logs, Results and
Status accept `providerRunId` (answering 404 when no `RUNNING` task has it, and 400 when it is ambiguous). The CLI does not
fall back to the other addresses. With an older DevOps service `complete-task` fails.

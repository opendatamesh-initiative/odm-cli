# odmcli platform devops complete-task

## Usage

`odm-cli platform devops complete-task --provider-run-id RUN_ID (--status STATUS | --results-only) [--logs-file FILE]... [--results-file FILE]`

## Description

Completes a running task, which it addresses by the pipeline's run id alone: no version, FQN, activity or task name is
needed.

**Instrumented task** (`--status`): the command uploads the logs, uploads the results, then records the final status,
always in that order.

**Full-control task** (`--results-only`): the pipeline reports its own status, so the command uploads only the results. The
run id is the pipeline's id, for example `$CI_PIPELINE_ID` on GitLab. In a child or downstream pipeline, pass the parent's
id down to the child and use it as the run id. When several full-control tasks of one pipeline run at the same time they must
not share the id: DevOps refuses an id that matches more than one `RUNNING` task (error 400).

Every local input is checked before any call to DevOps. A problem (a file that is missing or unreadable, results that are
not a JSON object, `--logs-file` with `--results-only`, `--results-only` without `--results-file`) is reported with all its
issues at once, and nothing is sent.

## Options

| Option              | Required | Description                                                                                          |
|---------------------|----------|------------------------------------------------------------------------------------------------------|
| `--provider-run-id` | yes      | Run id of the task: the value given to `start-task`, or the pipeline id of a full-control task.      |
| `--status`          | one of   | Final status of the instrumented task: `SUCCEEDED`, `FAILED` or `CANCELED`, in any case.             |
| `--results-only`    | one of   | Upload only the results of a full-control task. Exclusive with `--status`.                           |
| `--logs-file`       | no       | File uploaded as one log row. Repeatable. A blank file is skipped. Not allowed with `--results-only`. |
| `--results-file`    | no       | File with the results, one JSON object. Required with `--results-only`.                              |

Exactly one of `--status` and `--results-only` is required, otherwise the exit code is `2`. An empty run id, or one that is
still an unresolved variable (`$CI_PIPELINE_ID`, `${VAR}`, `$(cmd)`), is also a usage error (exit `2`).

## Partial failures

- **The results upload fails:** the status `FAILED` is recorded, whatever `--status` was given, and the command exits `1`.
- **Only the logs upload fails:** the requested status is recorded, and the command exits `1`.
- **The status is always attempted** in instrumented mode, once the local checks pass.

## When the run id is not found

The task may not be `RUNNING` yet, or may not have stored its run id yet (a full-control task, right after it starts).
DevOps answers that no `RUNNING` task has the run id, and the CLI retries the write `runIdLookupAttempts` times, every
`runIdLookupInterval` (default 5 times, every 2 seconds). After that it fails and names the run id, and suggests checking that
it matches the one given to `start-task` or, in a child pipeline, the parent's `CI_PIPELINE_ID`.

DevOps refuses a run id that matches more than one `RUNNING` task. This needs a DevOps service with run-id addressing (see
[the DevOps prerequisite](cmd-platform-devops.md#devops-prerequisite)).

## Example

```bash
odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --status SUCCEEDED \
  --logs-file build.log --results-file results.json

odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --results-only --results-file results.json
```

The results file is a JSON object:

```json
{"coverage": 80, "artifact": "registry.example.com/app:1.2.0"}
```

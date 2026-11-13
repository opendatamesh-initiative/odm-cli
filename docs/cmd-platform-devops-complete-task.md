# odmcli platform devops complete-task

## Usage

`odm-cli platform devops complete-task --provider-run-id RUN_ID [--status STATUS] [--logs-file FILE]... [--results-file FILE]`

## Description

Completes a running task, which it addresses by the pipeline's run id alone. The run id is the id of the pipeline run (`$CI_PIPELINE_ID` on GitLab, `github.run_id` on GitHub), for both kinds of
task.

`--status` selects the kind of task:

**Instrumented task**, with `--status`: the command uploads the logs, uploads the results, then records the final status,
always in that order.

**Full-control task**, without `--status`: the pipeline reports its own status, so the command uploads only the results,
and `--results-file` is required. The command logs that it is uploading only the results of a full-control task. The run
id is the pipeline's id, for example `$CI_PIPELINE_ID` on GitLab. In a child or downstream pipeline, pass the parent's id
down to the child and use it as the run id. When several full-control tasks of one pipeline run at the same time they must
not share the id: DevOps refuses an id that matches more than one `RUNNING` task (error 400).

Every local input is checked before any call to DevOps. A problem (a file that is missing or unreadable, results that are
not a JSON object, `--logs-file` without `--status`, no `--status` and no `--results-file`) is reported with all its
issues at once, nothing is sent, and the exit code is `1`.

## Options

| Option                    | Required | Description                                                                                                  |
|---------------------------|----------|--------------------------------------------------------------------------------------------------------------|
| `-r`, `--provider-run-id` | yes      | Run id of the task: the pipeline id, the same value given to `start-task`.                                   |
| `-s`, `--status`          | no       | Final status of an instrumented task: `SUCCEEDED`, `FAILED` or `CANCELED`, in any case. Without it, the task is a full-control task. |
| `-l`, `--logs-file`       | no       | File uploaded as one log row, for an instrumented task. Repeatable. A blank file is skipped. Needs `--status`. |
| `-o`, `--results-file`    | no       | File with the results, one JSON object. Required without `--status`.                                         |

A status that is not final, an empty run id, or one that is still an unresolved variable (`$CI_PIPELINE_ID`, `${VAR}`,
`$(cmd)`), is a usage error (exit `2`).

**Watch out:** an instrumented close job that forgets `--status` but passes `--results-file` uploads the results and
leaves the task `RUNNING`. The log says "no --status: uploading only the results of a full-control task".

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

# Full-control task: no --status, results only
odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --results-file results.json

# The instrumented close, with the short options
odm-cli platform devops complete-task -r "$CI_PIPELINE_ID" -s FAILED -l build.log
```

The results file is a JSON object:

```json
{"coverage": 80, "artifact": "registry.example.com/app:1.2.0"}
```

Complete pipelines, and how to close a task when a pipeline is cancelled, are in
[Run DevOps tasks from a pipeline](devops-pipelines.md).

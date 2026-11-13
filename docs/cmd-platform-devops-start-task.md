# odmcli platform devops start-task

## Usage

`odm-cli platform devops start-task --data-product-fqn FQN --data-product-version-number VERSION_NUMBER --activity STAGE --task TASK --provider-run-id RUN_ID [OPTIONS]`

## Description

Brings one instrumented task to `RUNNING`:

1. It looks for an open activity of the data product version and stage. If there is one it is reused, and Registry is not called.
2. If there is none, it looks the version up in Registry, checks that it is published, plans the activity from the stage of
   the descriptor's `lifecycleInfo`, and executes it.
3. It waits for the policy approval of the activity, requests the task with the pipeline's run id, and waits for the
   approval of the task.

The command exits `0` only when the activity and the task are `RUNNING`. Every wait is bounded by a timeout, and a timeout
writes nothing to DevOps. A refusal by DevOps (for example "Task build is not the next task") is shown verbatim.

Pass the same `--provider-run-id` to [`complete-task`](cmd-platform-devops-complete-task.md).

## Options

| Option                                | Required | Description                                                                                                              |
|---------------------------------------|----------|--------------------------------------------------------------------------------------------------------------------------|
| `-p`, `--data-product-fqn`            | yes      | Fully qualified name of the data product.                                                                                |
| `-n`, `--data-product-version-number` | yes      | Version number of the data product. It must equal the descriptor's `info.version`, which is the Registry `versionNumber`. |
| `-a`, `--activity`                    | yes      | Name of the activity: the stage of the descriptor's `lifecycleInfo`.                                                     |
| `-t`, `--task`                        | yes      | Name of the task to start (see [how the commands find the task](cmd-platform-devops.md#how-the-commands-find-the-task)). |
| `-r`, `--provider-run-id`             | yes      | Run id of the task: the id of the pipeline run, such as `$CI_PIPELINE_ID` (see [the run id](#the-run-id)).               |
| `--force-start`                       | no       | Replace an open activity that cannot accept the task, for manual re-runs (see [below](#--force-start)). It has no short form on purpose. |
| `--poll-interval`                     | no       | Interval between two polls while waiting, at least 1s. Default `cli.platform.devops.pollInterval` (5s).                  |
| `--activity-timeout`                  | no       | Maximum wait for the activity approval. Default `cli.platform.devops.activityApprovalTimeout` (30m).                     |
| `--task-timeout`                      | no       | Maximum wait for the task approval. Default `cli.platform.devops.taskApprovalTimeout` (10m).                             |

Required options that are empty, or still hold an unresolved variable such as `${VAR}`, `$VAR` or `$(cmd)`, are a usage
error (exit `2`).

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

## Publication check

A version is run only when it is `APPROVED` in Registry. A `PENDING` or `REJECTED` version is refused with a message naming
its state, and a version that is not in Registry fails. No option skips this check: running a version that is not
approved bypasses its policies, and the CLI does not offer it. Through the [Blindata Agent](devops-pipelines.md#connecting-to-odm)
the same rule is enforced on the server: only a DataOps admin can create an activity for a version that is not `APPROVED`.

## `--force-start`

`--force-start` unblocks a data product version and stage whose open activity cannot accept the task, typically because
the previous run was cancelled in the CI tool and left its task `RUNNING`.

**What it does:** it records the stale task `CANCELED`, cancels the activity if it is still open, and creates a new one (see
[open activities](#open-activities) for the cases it replaces).

**What it does not do:**

- it never runs a version that is not `APPROVED` (see [the publication check](#publication-check));
- it never skips the policy approval of the new activity: `start-task` still waits for the policy outcome.

**Who can use it:** through the Agent, the same user who can run the pipeline: DataOps editor, write on the data product's
team, and, to cancel and create the activity, a stage the Agent configuration allows (see [the pipeline account](devops-pipelines.md#the-pipeline-account)). With
direct, unauthenticated access to the services there is no permission check.

**When to use it:** for a manual re-run by an operator. **Never set `--force-start` in a pipeline file.** The CLI cannot
tell an abandoned run from a live one, so the flag can close the activity of a run that is still going.

## Getting the version

`--data-product-version-number` must be the version number the data product was published with. Two ways to give it to the pipeline:

- Read it from the descriptor in the checkout: `yq '.info.version' data-product-descriptor.yaml`.
- Otherwise, use a CI variable set by whoever triggers the pipeline.

## The run id

DevOps finds the task of `complete-task` by its run id, among the tasks that are `RUNNING`. Use the id of the pipeline run
(`$CI_PIPELINE_ID` on GitLab, `github.run_id` on GitHub), and pass the same value to `complete-task`. DevOps runs one task
of an activity at a time, so the tasks of one activity can share it.

One pipeline run must drive one activity: two tasks of different activities, running at the same time with the same run
id, could not be closed (DevOps refuses an ambiguous run id with error 400). The value for each provider is in
[what to pass on each provider](devops-pipelines.md#what-to-pass-on-each-provider).

## Example

```bash
odm-cli platform devops start-task --data-product-fqn urn:dpds:sales:dataproducts:orders:1 --data-product-version-number 1.2.0 \
  --activity dev --task build --provider-run-id "$CI_PIPELINE_ID"

# The same, with the short options
odm-cli platform devops start-task -p urn:dpds:sales:dataproducts:orders:1 -n 1.2.0 -a dev -t build -r "$CI_PIPELINE_ID"
```

Complete pipelines for GitLab CI and GitHub Actions, where `start-task`, the build and `complete-task` run in separate jobs,
are in [Run DevOps tasks from a pipeline](devops-pipelines.md).

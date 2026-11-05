# odmcli platform devops start-task

## Usage

`odm-cli platform devops start-task --data-product-fqn FQN --data-product-version VERSION --activity STAGE --task TASK --provider-run-id RUN_ID [OPTIONS]`

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

| Option                   | Required | Description                                                                                                                                          |
|--------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `--data-product-fqn`     | yes      | Fully qualified name of the data product.                                                                                                           |
| `--data-product-version` | yes      | Version of the data product. It must equal the descriptor's `info.version`, which is the Registry `versionNumber`.            |
| `--activity`             | yes      | Name of the activity: the stage of the descriptor's `lifecycleInfo`.                                                                                 |
| `--task`                 | yes      | Name of the task to start (see [how the commands find the task](cmd-platform-devops.md#how-the-commands-find-the-task)).                            |
| `--provider-run-id`      | yes      | Run id of the task: the id of the pipeline run, such as `$CI_PIPELINE_ID` (see [the run id](#the-run-id)). |
| `--force-start`          | no       | Operator override (see below).                                                                                                                       |
| `--poll-interval`        | no       | Interval between two polls while waiting, at least 1s. Default `cli.platform.devops.pollInterval` (5s).                                                     |
| `--activity-timeout`     | no       | Maximum wait for the activity approval. Default `cli.platform.devops.activityApprovalTimeout` (30m).                                                 |
| `--task-timeout`         | no       | Maximum wait for the task approval. Default `cli.platform.devops.taskApprovalTimeout` (10m).                                                         |

Required options that are empty, or still hold an unresolved variable such as `${VAR}`, `$VAR` or `$(cmd)`, are a usage
error (exit `2`).

## Publication check and `--force-start`

A version is run only when it is `APPROVED` in Registry. A `PENDING` or `REJECTED` version is refused with a message naming
its state. A version that is not in Registry fails in every case.

`--force-start` is an operator override for manual re-runs. It does two things:

- it runs a version that is not `APPROVED`, logging a warning;
- it replaces an open activity that cannot accept the task (see
  [open activities](cmd-platform-devops.md#open-activities)).

**Warning: never set `--force-start` in a pipeline file by default.** The CLI cannot tell an abandoned run from a live one,
so the flag can close the activity of a run that is still going.

## Getting the version

`--data-product-version` must be the version the data product was published with. Two ways to give it to the pipeline:

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
odm-cli platform devops start-task --data-product-fqn urn:dpds:sales:dataproducts:orders:1 --data-product-version 1.2.0 \
  --activity dev --task build --provider-run-id "$CI_PIPELINE_ID"
```

Complete pipelines for GitLab CI and GitHub Actions, where `start-task`, the build and `complete-task` run in separate jobs,
are in [Run DevOps tasks from a pipeline](devops-pipelines.md).

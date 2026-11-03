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
| `--provider-run-id`      | yes      | Run id of the task, for example the CI pipeline id. It must be unique among the tasks running at the same time (see [the run id](#the-run-id)). |
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

DevOps finds the task of `complete-task` by its run id, among the tasks that are `RUNNING`. When two jobs of one pipeline can
run instrumented tasks at the same time, the run id must be **unique per job**: for example `$CI_JOB_ID`, or the pipeline id
plus the job name. If two running tasks share a run id, DevOps refuses the close as ambiguous (error 400) and may not close the
right task. A run id shared by tasks that run one after the other (a multi-task stage in one job) is fine.

## GitHub Actions example

`complete-task` runs after a successful `start-task`, even when the steps in between fail (`if: always()` semantics), with the same run id.

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    env:
      # unique per job: github.run_id alone is shared by every job of the workflow run
      RUN_ID: ${{ github.run_id }}-${{ github.job }}
    steps:
      - uses: actions/checkout@v4
      - name: Read the data product version
        id: dp
        run: echo "version=$(yq '.info.version' data-product-descriptor.yaml)" >> "$GITHUB_OUTPUT"
      - name: Start the task
        id: start
        run: |
          odm-cli platform devops start-task \
            --data-product-fqn urn:dp:sales --data-product-version "${{ steps.dp.outputs.version }}" \
            --activity dev --task build --provider-run-id "$RUN_ID"
      - name: Build
        run: ./build.sh > build.log 2>&1
      - name: Complete the task (succeeded)
        if: success()
        run: odm-cli platform devops complete-task --provider-run-id "$RUN_ID" --status SUCCEEDED --logs-file build.log
      - name: Complete the task (failed)
        if: failure() && steps.start.outcome == 'success'
        run: odm-cli platform devops complete-task --provider-run-id "$RUN_ID" --status FAILED --logs-file build.log
      - name: Complete the task (canceled)
        if: cancelled() && steps.start.outcome == 'success'
        run: odm-cli platform devops complete-task --provider-run-id "$RUN_ID" --status CANCELED
```

`--status` accepts the three values in any case. The close steps run only when `start-task` succeeded, so a task that never
started is not closed.

## GitLab CI example

`complete-task` runs only when `start-task` succeeded: a marker file is written right after it. Without the marker, a job whose
`start-task` failed would still close a task by that run id. The run id is the job id, so parallel jobs never share it.

To run this job without installing the CLI, copy it into the build image from the [Docker image](./docker.md#gitlab-ci).

```yaml
build:
  script:
    - export VERSION="$(yq '.info.version' data-product-descriptor.yaml)"
    - odm-cli platform devops start-task --data-product-fqn urn:dp:sales --data-product-version "$VERSION"
        --activity dev --task build --provider-run-id "$CI_JOB_ID"
    - touch .odm-task-started
    - ./build.sh > build.log 2>&1
  after_script:
    - |
      if [ -f .odm-task-started ]; then
        case "$CI_JOB_STATUS" in
          success) STATUS=SUCCEEDED ;;
          canceled) STATUS=CANCELED ;;
          *) STATUS=FAILED ;;
        esac
        odm-cli platform devops complete-task --provider-run-id "$CI_JOB_ID" --status "$STATUS" --logs-file build.log
      fi
```

`after_script` runs when the job fails too. In a multi-job pipeline, a job with `when: always` can close the task of an earlier
job: it must be given that job's run id (for example `"$CI_PIPELINE_ID-build"` in both jobs), and it must only run when the
`start-task` job succeeded.

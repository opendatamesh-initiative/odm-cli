# Run DevOps tasks from a pipeline

How to write a GitLab CI or GitHub Actions pipeline that runs the tasks of a DevOps activity with
[`start-task`](cmd-platform-devops-start-task.md) and [`complete-task`](cmd-platform-devops-complete-task.md).

## How it works

A task of a DevOps activity runs in one of two modes:

- **Instrumented**: your pipeline runs the work, and the CLI keeps DevOps in sync. `start-task` waits until DevOps
  approves the task, your build runs, and `complete-task` uploads the logs and the results and records the final status.
- **Full control**: DevOps starts the pipeline through its executor and reads the pipeline's status itself. The pipeline
  only uploads its results, with `complete-task --results-only`.

In both modes:

- **Each CLI call runs in its own job**, with the [CLI image](run-with-docker.md) as the job image. Your build job keeps
  its own image: the CLI never has to be installed in it.
- **The run id is the id of the whole pipeline run**, not of a job: `$CI_PIPELINE_ID` on GitLab, `github.run_id` on
  GitHub. Every CLI call of the pipeline passes the same value as `--provider-run-id`. DevOps finds the task by this id
  among the `RUNNING` tasks. DevOps runs one task of an activity at a time, so the tasks of one activity can share the id.
- **One pipeline run drives one activity.** If one pipeline ran tasks of two activities at the same time (for example two
  data products in a monorepo), both tasks would have the same run id, and DevOps would refuse to close them as ambiguous.
- **Set the service URLs once**, as CI variables shared by all your pipelines (GitLab group or project CI/CD variables,
  GitHub organization or repository variables). The CLI reads them from the environment:
  `CLI_PLATFORM_SERVICES_DEVOPS_URL`, `CLI_PLATFORM_SERVICES_REGISTRY_URL`, and the credentials if your platform requires
  them (see [Environment variables](../README.md#environment-variables)). Store credentials as masked variables or secrets.
  On GitLab, do not mark them **Protected** unless the branches and tags your pipelines run on are protected: protected
  variables reach only those pipelines. Full-control pipelines run on the version tag, so without protected tags the
  jobs get no URL and the CLI fails naming the missing variable.

## What to pass on each provider

| What | GitLab CI | GitHub Actions |
| --- | --- | --- |
| `--provider-run-id` | `$CI_PIPELINE_ID` | `${{ github.run_id }}`: the workflow run id, **not** the job id. It is the same for every job of the run, and kept on re-runs. |
| Work in a child or downstream pipeline | The parent's id, passed down as a variable (for example `PARENT_PIPELINE_ID: $CI_PIPELINE_ID`): the child's `CI_PIPELINE_ID` is different. | A called reusable workflow shares the caller's `github.run_id`. A workflow started by `workflow_run` has its own id: pass `github.event.workflow_run.id`. |
| `--data-product-version` | A CI/CD variable, set by whoever triggers the pipeline, or read from the descriptor in an earlier job and passed on as a dotenv artifact | A workflow input or variable, or a step output |
| Build outcome → `--status` | Two close jobs: `when: on_success` → `SUCCEEDED`, `when: on_failure` → `FAILED` | `needs.<build job>.result`: `success` → `SUCCEEDED`, `failure` → `FAILED`, `cancelled` → `CANCELED` |
| Close only if `start-task` succeeded | A dotenv artifact written by the start job | `needs.<start job>.result == 'success'` |
| Logs and results between jobs | `artifacts:` with `when: always` | `actions/upload-artifact` and `actions/download-artifact` |
| Service URLs and credentials | CI/CD variables, masked for secrets | Organization or repository `vars` and `secrets`, mapped in the workflow `env:` |
| Full-control tasks | `$CI_PIPELINE_ID`: the GitLab executor stores it as the task's run id | Not available yet: DevOps has no GitHub executor. |

The data product version must equal the descriptor's `info.version` of the published version (see
[getting the version](cmd-platform-devops-start-task.md#getting-the-version)). The CLI image has no `yq`: read the version
in a job with your own image, or pass it as a variable.

## GitLab CI

### Instrumented task

The task named `build` of the `dev` stage, with its work done by the `build` job:

```yaml
stages: [odm-start, build, odm-complete]

variables:
  ODM_CLI_IMAGE: opendatamesh/odm-cli:X.Y.Z
  # CLI_PLATFORM_SERVICES_DEVOPS_URL and CLI_PLATFORM_SERVICES_REGISTRY_URL are group or project CI/CD variables

odm-start-task:
  stage: odm-start
  image: $ODM_CLI_IMAGE
  script:
    - odm-cli platform devops start-task --data-product-fqn urn:dpds:sales:dataproducts:orders:1
        --data-product-version "$DP_VERSION" --activity dev --task build --provider-run-id "$CI_PIPELINE_ID"
    - echo "ODM_TASK_STARTED=true" > odm.env
  artifacts:
    reports:
      dotenv: odm.env   # uploaded only when start-task succeeded

build:
  stage: build
  image: maven:3-eclipse-temurin-21   # your build image, unchanged
  script:
    - set -o pipefail
    - ./build.sh 2>&1 | tee build.log   # writes results.json
  artifacts:
    when: always
    paths: [build.log, results.json]

odm-complete-succeeded:
  stage: odm-complete
  image: $ODM_CLI_IMAGE
  when: on_success
  script:
    - odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --status SUCCEEDED
        --logs-file build.log --results-file results.json

odm-complete-failed:
  stage: odm-complete
  image: $ODM_CLI_IMAGE
  when: on_failure
  script:
    - '[ "$ODM_TASK_STARTED" = true ] || { echo "start-task did not succeed: no task to close"; exit 0; }'
    - '[ -e build.log ] || touch build.log'   # a missing file fails the local checks; the artifact may be read-only
    - odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --status FAILED --logs-file build.log
```

- `odm-complete-failed` runs when any earlier job failed. The dotenv variable tells it whether `start-task` succeeded:
  if it did not, there is no task to close.
- For a stage with several tasks, repeat the start / build / complete jobs for each task, in the stage order. Every task
  uses the same `$CI_PIPELINE_ID`.
- `start-task` and the build must run in separate stages, so the build never starts before the task is approved.
- If your jobs use `rules`, put `when: on_success` and `when: on_failure` inside the rule: GitLab rejects a job-level
  `when` together with `rules`.

### Full-control task

DevOps starts the pipeline and reads its status through the executor. The pipeline uploads its results:

```yaml
upload-results:
  image: opendatamesh/odm-cli:X.Y.Z
  script:
    - odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --results-only --results-file results.json
```

`results.json` comes from an earlier job, as an artifact. In a child or downstream pipeline, pass the parent's
`CI_PIPELINE_ID` down and use it instead.

## GitHub Actions

> These examples have not been verified on GitHub yet.

### Instrumented task

```yaml
env:
  ODM_RUN_ID: ${{ github.run_id }}   # the workflow run id, not the job id
  CLI_PLATFORM_SERVICES_DEVOPS_URL: ${{ vars.ODM_DEVOPS_URL }}
  CLI_PLATFORM_SERVICES_REGISTRY_URL: ${{ vars.ODM_REGISTRY_URL }}

jobs:
  odm-start-task:
    runs-on: ubuntu-latest
    container: opendatamesh/odm-cli:X.Y.Z
    steps:
      - run: >
          odm-cli platform devops start-task --data-product-fqn urn:dpds:sales:dataproducts:orders:1
          --data-product-version "${{ vars.DP_VERSION }}" --activity dev --task build --provider-run-id "$ODM_RUN_ID"

  build:
    needs: odm-start-task
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: |
          set -o pipefail
          ./build.sh 2>&1 | tee build.log   # writes results.json
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: odm-task-output
          path: |
            build.log
            results.json

  odm-complete-task:
    needs: [odm-start-task, build]
    # always(): runs also when the build failed or the run was cancelled, but only for a started task
    if: always() && needs.odm-start-task.result == 'success'
    runs-on: ubuntu-latest
    container: opendatamesh/odm-cli:X.Y.Z
    steps:
      - uses: actions/download-artifact@v4
        continue-on-error: true
        with:
          name: odm-task-output
      - run: |
          case "${{ needs.build.result }}" in
            success) STATUS=SUCCEEDED ;;
            cancelled) STATUS=CANCELED ;;
            *) STATUS=FAILED ;;
          esac
          [ -e build.log ] || touch build.log
          RESULTS=""
          if [ "$STATUS" = SUCCEEDED ] && [ -f results.json ]; then RESULTS="--results-file results.json"; fi
          odm-cli platform devops complete-task --provider-run-id "$ODM_RUN_ID" --status "$STATUS" --logs-file build.log $RESULTS
```

The image runs as a non-root user (UID 10001). If a step cannot write in the workspace, add `options: --user 1001` to the
`container:` of that job.

### Full-control task

Not available yet: DevOps has no GitHub executor, so it cannot start a GitHub workflow.

## Cancelling a run

| Situation | What happens | What to do |
| --- | --- | --- |
| Full control, cancelled in DevOps or in the CI | DevOps cancels the pipeline, or reads its status, through the executor. | Nothing. |
| Instrumented, cancelled in the DevOps UI while the pipeline runs | DevOps cancels the pending tasks and waits for the running task's status. The pipeline's close job records it, and the activity ends `CANCELED`. | Nothing, as long as the pipeline is not also cancelled. |
| Instrumented, GitHub run cancelled | The close job (`if: always()`) still runs, with `needs.build.result == 'cancelled'`, and records `CANCELED`. | Nothing. |
| Instrumented, GitLab pipeline cancelled | The `on_success` and `on_failure` jobs do not run on a cancelled pipeline. **The task stays `RUNNING`**, and it blocks its activity. | Close the task by hand, or let an operator force the next run (below). |

To close a task left `RUNNING`, record its status with the run id of the cancelled pipeline:

```bash
docker run --rm -e CLI_PLATFORM_SERVICES_DEVOPS_URL=https://odm.example.com/devops \
  opendatamesh/odm-cli:X.Y.Z platform devops complete-task --provider-run-id <pipeline id> --status CANCELED
```

An operator can also run the next pipeline with `start-task --force-start`, which closes the stale task and replaces the
activity (see [open activities](cmd-platform-devops.md#open-activities)). Never set `--force-start` in the pipeline file.

## Re-runs and retries

- **Retry the whole pipeline**, or from the start job. Retrying only the build job does not run `start-task` again, so
  after a `FAILED` close there is no running task left to complete.
- GitLab job retries and GitHub "re-run jobs" keep the run id. A re-run `start-task` that finds the earlier task still
  `RUNNING` is refused: close it first, as above.

## Troubleshooting

| Symptom | Cause |
| --- | --- |
| `complete-task` fails after retrying: no `RUNNING` task with the run id | A different run id than the one given to `start-task`, a child pipeline that used its own `CI_PIPELINE_ID`, or a task already closed. |
| DevOps refuses the run id as ambiguous (error 400) | Two `RUNNING` tasks have the same run id: one pipeline run is driving two activities at once. |
| `start-task` is refused: the task is not `PENDING` | The task is still `RUNNING` from an earlier run that never closed it (see [Cancelling a run](#cancelling-a-run)). |
| A command fails naming `CLI_PLATFORM_SERVICES_DEVOPS_URL` | The service URL is not set in the job's environment or configuration file. |

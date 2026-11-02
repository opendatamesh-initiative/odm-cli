# SPDD Analysis: DevOps V2 CLI Commands (instrumented path and results upload)

Story **BDMD-5443**. DevOps server counterpart: **BDMD-5442** (instrumented path, on `odm-platform-pp-devops-server` `main`). UI counterpart for the activity mapping: **BDMD-5518** (`blindata-ui`).

**Status (2026-11-02).** No questions are open. The design below is implemented:
- **CLI:** `odm-cli`, REASONS `spdd/prompt/BDMD-5443-202610291156-[Feat]-commands-devops-v2-start-complete-task.md`.
- **DevOps:** run-id addressing in `odm-platform-pp-devops-server` (DD-17).

Both were reviewed and tested end to end on a local platform with a GitLab pipeline, in full-control and instrumented mode. The DevOps integration tests still have to run where Docker is available. The CLI Docker image has its own analysis (`BDMD-5443-202610270941-[Analysis]-cli-docker-image.md`) and is out of this document.

## Original Business Requirement

### The story

The ODM CLI runs inside the customer's pipeline (for example GitHub Actions or GitLab CI). There it keeps DevOps V2 in sync for **instrumented** tasks (BDMD-5442), and it uploads results for **full-control** tasks. The command structure was discussed in the "DevOps v2 CLI" call of 2026-10-23.

The CLI's responsibilities, as the story states them:
- Execute the activity and poll it until it is `RUNNING` or `FAILED`. That is how the policy outcome arrives.
- For each task, request its execution by name with the pipeline's provider run id, then poll it until it is `RUNNING` or `FAILED`.
- Upload the logs, then the results, and the terminal status last.
- When DevOps refuses a request (the task is not next, or another task is running), show the error as it is and fail the pipeline step.

The first hypothesis was four commands under a new `platform devops` group: `validate`, `upload-results`, `upload-logs` and `complete-task-execution`. The names were provisional, and how tasks would be started was still open.

**Acceptance criteria** (translated from the story):

1. Execute the activity and poll until it is running or failed.
2. Request the task by name with the provider run id, and poll until it is running or failed.
3. Upload in this order: logs, results, status.
4. A DevOps ordering or concurrency error is shown plainly, with an exit code other than 0.
5. The commands are under `platform devops`: `validate` (polls until the policy approves), upload results, upload logs, and a final status command with a defined name.
6. The placement of the commands is decided and documented: directly under `devops`, or in an `executor` subgroup.
7. A Docker image of the CLI can be used in pipelines instead of the executable, with the same commands. This has its own analysis.

**Out of scope:** the DevOps-side logic (BDMD-5442), and mixed activities with instrumented and full-control tasks.

**Open in the story:**
- Which descriptor data the CLI needs in instrumented mode. Gabriele's direction: the CLI receives the FQN and the version, then fetches the descriptor from the registry.
- Which parameters the pipeline must pass.
- The Docker packaging details.

**Sources:**
- The story notes `~/git/tmp/BDMD-5443.md` and `~/git/tmp/BDMD-5443-commands.md`. They were generated from call transcripts and may be inaccurate.
- Knowledge PRs #22, #23 and #36 in `blindata/knowledge`: the call sources, `execution-mode`, `advance-activity-and-execute-task-branch-on-execution-mode` and `cli-starts-only-first-pending-task-and-fails-clearly`.

### Decision history

The analysis went through seven revisions with the team. The decisions themselves live in [Key Design Decisions](#key-design-decisions) and [Risk & Gap Analysis](#risk--gap-analysis).

| Rev. | Date | Decisions |
| ---- | ---- | --------- |
| 1 | 2026-10-26 | First draft with five commands (`validate`, `start-task`, `upload-logs`, `upload-results`, `complete-task`). Superseded. |
| 2 | 2026-10-27 | **Team:** only `start-task` and `complete-task`, with the complexity kept inside the commands. Failures are explicit through the exit code. Every parameter is passed explicitly; mapping CI environment variables is kept as an alternative only. The descriptor was to be passed as a file (replaced in rev. 4). |
| 3 | 2026-10-28 | **Team:** accepted the recommended answers to Q4, Q7, Q8, Q9, Q11, Q13, Q14, Q18, Q19 and Q23. |
| 4 | 2026-10-28 | The CLI calls Registry V2 itself for the version uuid and the published descriptor, instead of reading a descriptor file (Q16, Q17, Q22, DD-13). V1 and V2 Registry calls share one client (DD-14). No authentication design in this story (Q15). |
| 5 | 2026-10-29 | The pipeline passes the FQN, version, activity and task. The descriptor is used only to create the activity, and nothing is returned to the pipeline (DD-04, DD-15). |
| 6 | 2026-10-29 | **Team:** the registry call stays in the CLI. `start-task` refuses a version that is not `APPROVED` unless `--force-start` is passed (DD-16). The stage restriction applies to the UI only (Q21). DevOps' Record Task Logs, Results and Status accept the provider run id as the task address, so `complete-task` needs only the run id (DD-17, Q12). |
| 7 | 2026-10-29 | **Team:** one `--force-start` flag for both overrides (Q6). `--task` stays required (Q25). The version is always passed explicitly, from the descriptor in the checkout or from a CI variable; a lookup by git tag was rejected (Q24). |
| 8 | 2026-11-02 | Aligned with the implementation, the review and the local end-to-end tests. Implementation details are added to DD-05, DD-07, DD-12, DD-14 and DD-17. The rule that an instrumented run id must be unique per CI job is added. |

## Domain Concept Identification

### Existing Concepts (from codebase)

**odm-cli**

- **Command tree:** `odm-cli` → `platform` → `registry` → `validate`, built by `PicoCliCommandBuilder` beans that attach to their parent by name. A `devops` group attaches to `platform` the same way, next to `registry`.
- **Command executor:** `PicoCliCommandExecutor` resolves required options (prompting on the console when `interactive` is true), runs one use case, and maps any exception to exit code `1`. Exit codes are `0` success, `1` command failure, `2` invalid usage (2.0.0).
- **Use case slices:** command → factory → use case → outbound ports → presenter, as in `DescriptorValidator` (`usecases/descriptorvalidator`).
- **Registry client** (`clients/platform/registry`, see `agents-reports/BDMD-5443-registry-v2-client-scout.md`):
  - **Endpoint:** `PlatformRegistryClientImpl` calls one endpoint, the V1 `POST {url}/api/v1/pp/registry/validate/report`. It has no V2 calls.
  - **Configuration:** `cli.platform.services.registry.url` is the base address only. The client appends the full `/api/...` path, so V2 paths can use the same `url`. The `headers` (API key) and `oauth2` keys are bound through `PlatformServiceCredentials`.
  - **Construction:** `PlatformRegistryClientFactory` builds the client lazily with `RestUtilsFactory.getAuthenticatedRestUtils`.
  - **Errors:** `ClientException` is translated into `InternalException`.
  - **Tests:** `MockRestServiceServer`.
- **HTTP utilities:** `utils/client/**` and `exceptions/client/**` are byte-identical to template 1.1.1 (commits `22ca185`, `882be25`, `d43278c`). Pagination (`RestUtils.getPage`, `PageUtility`) is available and matches the Spring Boot 3.5 page JSON of the servers. Small deviations from the INTEGRATIONS norm remain:
  - a lazy factory instead of a qualified `RestUtils` bean;
  - a hardcoded 180 s timeout (`RestTemplateConfigs`);
  - no check for a blank `url`;
  - field injection in the factory.
- **Remote errors:** `ClientException` carries the HTTP status and the raw response body. Its message today is `{code=…, responseBody='…'}`.
- **Process model:** every command is a separate JVM run. The CLI keeps no state between two pipeline steps.

**DevOps V2 server (BDMD-5436/5437/5440/5442, verified in code)**

- **Activity:** one run of a named activity on a data product version. Identified by a uuid, and for the CLI by the natural key **data product FQN + version tag + activity name**, which only matches `PENDING` or `RUNNING` activities. At most one open activity per data product version uuid and name.
- **Task:** a unit of an activity, ordered by `sortOrder`, addressed by name. Its executor name selects a mode declared in the server configuration (`full-control` or `instrumented`, e.g. an executor called `cli`). The mode is not exposed by any read. A task holds the `providerRunId`, logs and results.
- **Execution status:** `PENDING`, `RUNNING`, `SUCCEEDED`, `FAILED`, `CANCELED`, shared by activity and task.
- **Policy approval:** asynchronous and internal to DevOps (Policy V1 or auto-approval). It is visible only through status: `RUNNING` means approved, `FAILED` means rejected, with no stored reason.
- **DevOps commands the CLI calls:** Execute Activity, Request Task Execution, Record Task Logs, Record Task Results, Record Task Status, plus the activity reads (by uuid with tasks, search by natural key without tasks). The search filters only on version uuid, FQN, version tag, name and statuses.
- **Task address on the task endpoints:** `{activityUuid, taskName}` or `{dataProductFqn, dataProductVersionTag, activityName, taskName}`. The name must match exactly one task. The server does not enforce unique task names; the UI and the CLI do.
- **Mode checks:** Record Task Logs and Record Task Status refuse a task that is not instrumented ("Task X is not instrumented"). Record Task Results accepts both modes.
- **Task results:** text rows appended to a task while it is `RUNNING`. A succeeded task's results feed the `${<activity>.results.<task>.<path>}` placeholders of later full-control tasks, which read only JSON objects.
- **Placeholders:** resolved only for full-control tasks, just before the executor start call (`ExecuteTask`). `${<activity>.results.<task>.<path>}` is the only resolved syntax; anything else stays as written, with a warning. No built-in placeholders exist.
- **Executor start command:** `POST {executor}/api/v2/up/executor/tasks/start` with only `executorParameters` and the resolved `pipelineParameters`, plus the executor secrets as headers. There is no activity uuid, task uuid, task name, activity name, FQN or version tag, and no callback. The executor returns the `providerRunId`, which DevOps stores after the call returns.
- **Cancel:** cancel does not close a `RUNNING` instrumented task (`CancelActivity.leaveInstrumentedTaskRunning`). It cancels the pending tasks, then waits for Record Task Status. If nothing reports, it fails with "did not reach a terminal status". Only Record Task Status closes an abandoned instrumented task.
- **Refused task request:** a refused Request Task Execution changes no state. The causes are activity not `RUNNING`, task not `PENDING`, task not next, and another task running.
- **Instrumented task on Execute Activity:** needs only a name and a declared instrumented executor name. Executor parameters, pipeline parameters and secrets are optional for it.
- **Provider run id** (verified 2026-10-29). A task column, `activities_tasks.provider_run_id`, indexed but not unique (`V1__init_schema.sql`). Execute Activity creates every task with a null run id (`ExecuteActivity.prepareForExecution`), so a new activity never inherits one. It is written in two ways:
  - **Instrumented:** by Request Task Execution, while the task is still `PENDING`, from the value the CLI sends. A repeated request overwrites it.
  - **Full control:** by `ExecuteTask.recordProviderRunId`, in its own transaction, after the executor start call returns. The task is already `RUNNING` before that, so for a short time a running task has no run id. `CancelActivity.waitForProviderRunId` already waits for this gap.

  Before this story no endpoint took it as an address: the record commands (`RecordTaskResultsCommand`, `RecordTaskLogsCommand`, `RecordTaskStatusCommand`) took only the activity uuid (results only) or the natural key, plus the task name. DD-17 adds the run id as a third address.

**GitLab executor (`odm-platform-adapter-devops-executor-gitlab`)**

- **Pipeline parameters become GitLab CI inputs (`spec:inputs`), not variables.** The executor reads the inputs the pipeline's CI configuration declares at the ref. It sends only those, and drops the other parameters with an INFO log (`StartTask.java:85-125`). The variables channel was removed on purpose (BDMD-5099).
- **What the pipeline sees:** its declared inputs, plus GitLab's predefined variables (`CI_PIPELINE_ID`, which equals the `providerRunId`, `CI_PIPELINE_SOURCE=api`, the ref). The executor itself knows none of the activity or task coordinates.

**UI (BDMD-5518, `planDevOpsV2Execution` in `blindata-ui`)**

- **Activity mapping:** the UI builds the Execute Activity body from the Registry V2 data product and release (version):
  - **Version uuid:** the release uuid.
  - **FQN:** the data product's `fqn`.
  - **Version tag:** the descriptor's `info.version`.
  - **Activity name:** the stage the user picks among the keys of `internalComponents.lifecycleInfo`.
  - **Activity sort order:** the 0-based position of that stage key.
  - **Tasks:** that stage's entries, ordered by `order` (or position).
- **Task fields:** each task is built from its descriptor entry:
  - **Name:** `name`, else `template.name`, else `task_<order>`. It must be unique.
  - **Description:** `template.description`.
  - **Executor name:** `service.$href`.
  - **Executor parameters:** built from `template.definition` and the Registry repositories, tags and branch.
  - **Pipeline parameters:** `configurations.params`, stringified.
- **Blocking errors:** an unknown stage, no tasks, a missing executor, or a duplicate task name. A missing repository, ref or tag is also blocking; only full-control executor parameters need those.
- **Inputs from the user:** the stage, and the executor secrets (resolved by the Agent proxy).

**Registry server**

- **Lookup:** a data product version is found with `GET /api/v2/pp/registry/products?fqn=` → `GET /api/v2/pp/registry/products-versions?dataProductUuid=&versionNumber=` → `GET /api/v2/pp/registry/products-versions/{uuid}` (descriptor `content`).
- **Uuid:** generated at random (`@GeneratedValue(strategy = UUID)`). It is not in the descriptor, and nobody can compute it.
- **Publication state:** each version has a `validationState`: `PENDING` when publication is requested, then `APPROVED` or `REJECTED` by policy (`DataProductVersionPublisher`, `DataProductVersionApprover`, `DataProductVersionRejector`). It is in both the search result and the full version. A version is "published" when it is `APPROVED`.
- **`versionNumber`:** extracted from the descriptor's `info.version` at publish time. Registry's `tag` is the release's git tag, used as the executor `ref`. So DevOps' "version tag" is Registry's `versionNumber`, not Registry's `tag`.
- **Reference client:** `odm-platform-pp-blueprint-server` is the only project that calls Registry V2. It runs the same three-call lookup:
  - **Classes:** `RegistryClientImpl`, the `Registry*SearchOptions` and `Registry*Res` classes, and the caller `ReconstructPublicationRequestedService`.
  - **Duplicates:** it calls `getPage(Pageable.ofSize(2))` to detect them.
  - **Divergences:** it has no auth, and its RestUtils copy is older than the template's.

**Agent proxy (`blindata-agent`, `OdmDevopsV2ServiceAuthFilter`)**

- **Execute:** needs `DATAOPS_EDITOR`, write permission on the data product's team, and the stage regex `canDeployOnlyStage`.
- **Reads:** need `DATAOPS_VIEWER`.
- **Everything else** (task request, logs, results, status) falls to the catch-all handler and needs `DATAOPS_ADMIN`.

DevOps itself checks none of these. The task endpoints check only the role, not a data product, so a request addressed by run id passes the proxy as it is.

### New Concepts Required

- **DevOps command group:** `odm platform devops`, with its own client and configuration entry.
- **`start-task`:** one command that brings one instrumented task to `RUNNING`. It finds the open activity, or else fetches the published descriptor from Registry and executes the activity planned from it. It waits for activity approval, requests the task with the provider run id, and waits for task approval. Success means the pipeline may run the task's work.
- **`complete-task`:** one command that closes one task. It uploads the logs, then the results, then records the terminal status. With `--results-only` (full control) it uploads results only. It addresses the task by its provider run id.
- **Force start** (`--force-start` on `start-task`): the operator's override. It lets `start-task` run a version that is not `APPROVED`, and close an open activity that blocks the task (Q6).
- **Task address by provider run id** (DevOps change, DD-17): the `RUNNING` task whose run id equals the one given. It replaces the activity key and task name on the three record endpoints.
- **Version lookup:** the Registry V2 lookup from FQN and version number to the version uuid and the descriptor content. It is added to the existing Registry client, next to the V1 validate call.
- **Activity plan:** the Execute Activity body the CLI derives from the descriptor content with the UI's rules, restricted to what an instrumented task needs.
- **Task coordinates:** data product FQN, version (descriptor `info.version`), activity name and task name. `start-task` needs them to find or create the activity. After that, the provider run id is enough.
- **Wait (poll until a state):** a CLI-side wait on the activity read, with an interval and a timeout. `start-task` waits twice: for the activity, then for the task, each until `RUNNING` or `FAILED`.
- **Failure outcome:** the explicit reason a command failed, carried by the exit code and the last log line. The causes are a usage error, a policy rejection (activity or task), a DevOps refusal, a Registry lookup failure, a timeout, or an unreachable service.

### Key Business Rules

- **Approval comes before work.** `start-task` succeeds only when both the activity and the task are `RUNNING`. `FAILED` on either wait fails the command, and with it the pipeline step.
- **The CLI may request only the next pending instrumented task, and only while no other task is `RUNNING`.** DevOps enforces this and refuses with a 400 and a message. The CLI shows that message as it is and fails. The CLI does not re-implement the check.
- **The pipeline is the source of the task coordinates.** The FQN, version, activity name and task name are options the pipeline passes to `start-task`. `complete-task` needs only the run id. The CLI never derives them from the descriptor and never returns them. When `start-task` creates the activity, it sets `dataProductFqn`, `dataProductVersionTag` and `name` from those options. It also takes the stage named by `--activity` from the descriptor. So the activity's natural key in DevOps equals the options by construction, and every later call with the same options finds it.
- **The descriptor is needed once per activity run.** It is used only to plan the activity, when none is open. Once the activity exists, DevOps holds its tasks, order and executors, and later calls need only the coordinates.
- **The activity is created once.** `start-task` reuses the open activity with the same FQN, version and activity name. It executes a new activity only when there is none. Registry is called only in that case.
- **Only published versions run, unless forced.** The activity is planned from the descriptor of the Registry version, never from a local file. A version that is not in Registry cannot be run, even with `--force-start`: there is no uuid and no descriptor. A version that is in Registry but not `APPROVED` (`PENDING` or `REJECTED`) is refused unless `--force-start` is passed (DD-16).
- **The run id addresses the running task.** Once a task is `RUNNING`, the pipeline addresses it by the run id alone. For GitLab full-control tasks that is `CI_PIPELINE_ID`. For instrumented tasks the pipeline chooses the id, and it must be unique per CI job (`$CI_JOB_ID` on GitLab, `github.run_id-github.job` on GitHub), so that parallel jobs never share one. DevOps matches only `RUNNING` tasks and refuses an ambiguous id (DD-17).
- **Repeating a task request is allowed** while the task is still the next `PENDING` one. It overwrites the provider run id, so a retried `start-task` is safe.
- **The upload order is logs → results → status, inside one command.** Logs and results are accepted only while the task is `RUNNING`, and the status ends `RUNNING`, so it is always last.
- **The status always gets written.** A task left `RUNNING` blocks the activity and every later run with the same name. Cancel does not close an instrumented task, and DevOps has no timeout. `complete-task` writes the status even when the work failed.
- **Logs and status are instrumented-only; results are for both modes.**
- **Results should be a JSON object.** Only JSON objects resolve placeholders.
- **FQN and version must be sent on Execute Activity.** The server does not require them, but every later instrumented call looks the activity up by them.
- **Task names come from the mapping.** The `--task` value must equal the derived name (`name`, else `template.name`, else `task_<order>`), and names must be unique in the stage.
- **The stage's executor references must name instrumented executors declared in DevOps** (for example `cli`). The CLI does not fill full-control executor parameters or secrets.

## Strategic Approach

### Solution Direction

Add a `devops` command group under `platform` with two commands, `start-task` and `complete-task`. Each is one use case that orchestrates several remote calls behind outbound ports, following the `platform registry validate` slice and the template norms. The complexity the team accepted lives in the use cases, as named steps (CODE_STRUCTURE and PROGRESSIVE_DISCOVERY norms), not in the pipeline. Extend the existing Registry client with the V2 version lookup. Add a DevOps client with the same configuration shape.

`start-task` procedure:
1. Find the open activity.
2. Only if there is none:
   1. Look up the version in Registry (uuid, validation state and content).
   2. Refuse it if it is not `APPROVED`, unless `--force-start`.
   3. Plan the activity.
   4. Execute it.
3. Wait for the activity.
4. Request the task.
5. Wait for the task.

`complete-task` procedure: check local inputs → upload logs → upload results → record status. With `--results-only`, only the results upload runs.

Pipeline usage, one task per step group (illustrative):

```text
start:     odm platform devops start-task    --data-product-fqn … --data-product-version … --activity … --task … --provider-run-id …
work:      <the task's own steps>                                                     (only if start succeeded)
complete:  odm platform devops complete-task --provider-run-id … --status … [--logs-file …] [--results-file …]
                                                                                              (always, if start succeeded)
```

A full-control pipeline started by a DevOps executor calls `complete-task --results-only --provider-run-id …` while its task is `RUNNING`, with its own run id (`CI_PIPELINE_ID` on GitLab). It needs no coordinates, so a pipeline shared by several tasks or stages works too (Q12).

| Command                       | Registry and DevOps calls                                                                                                                                  | Waits                                                                     | Modes        |
| ----------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ------------ |
| `start-task`                  | search open activity → if none: Registry products by FQN → versions by product and version number → version content; Execute Activity → read activity → Request Task Execution → read activity | Activity until `RUNNING` / `FAILED`, then task until `RUNNING` / `FAILED` | Instrumented |
| `complete-task`               | Record Task Logs → Record Task Results → Record Task Status                                                                                                | No                                                                        | Instrumented |
| `complete-task --results-only` | Record Task Results                                                                                                                                        | No                                                                        | Full control |

### Key Design Decisions

- **DD-01: Scope** _(revised)_. In:
  - the `platform devops` group with `start-task` and `complete-task`;
  - the DevOps client and its configuration;
  - the Registry V2 version lookup in the existing Registry client;
  - the descriptor-to-activity mapping, restricted to instrumented tasks;
  - polling with timeouts;
  - failure outcomes and exit codes;
  - the documentation under `docs/`, including pipeline examples;
  - the publication check and `--force-start` (DD-16).

  Server prerequisite, planned here and implemented in `odm-platform-pp-devops-server`: the task address by provider run id (DD-17).

  Out:
  - the Docker image (separate analysis);
  - executor changes (none needed);
  - full-control executor parameters and secrets on the CLI's Execute Activity;
  - activity cancel and other user-facing commands;
  - authorization and the stage restriction (Q21, not applicable to pipelines);
  - the DevOps follow-up that lets a UI cancel close an abandoned instrumented task (Q6).

- **DD-02: Two commands, each owning one side of the task's work** _(team, rev. 2)_. `start-task` is everything before the work, `complete-task` everything after it. _Trade-off:_ each command does several remote calls and can fail half-way. The use case reports which step failed (DD-07), and each step is safe to retry.
- **DD-03: Commands sit directly under `platform devops`** (Q4, closed). With two commands, an `executor` subgroup adds a level without its own options or behaviour. The CLI is not a DevOps executor in the server's sense either.
- **DD-04: `start-task` is get-or-create on the activity.** It looks for an open activity by FQN, version and activity name, all three from its options. It goes to Registry and executes a new activity only when none is open. A race between two runs is resolved by the server guard: on that refusal the CLI looks the activity up again. _Trade-off:_ an open activity left by an abandoned run is reused, and may refuse the task. With `--force-start` the CLI replaces it instead (Q6). In a multi-task stage, only the first task's `start-task` normally reaches Registry. The others find the activity. Two steps creating it at the same time both fetch the descriptor, and one of them then reuses the other's activity.
- **DD-05: The CLI waits by reading the activity.** Both waits read the activity by uuid (tasks included) and match the task by name. The interval is 5 s, with a 30 min timeout for the activity and 10 min for the task. On timeout the command fails and writes no DevOps state (Q7).
  - **Options:** `--poll-interval` (at least 1 s), `--activity-timeout` and `--task-timeout`.
  - **Configuration:** `cli.platform.devops.pollInterval`, `activityApprovalTimeout` and `taskApprovalTimeout`.
  - **Progress:** printed at the first poll and then once a minute. Every poll is logged at DEBUG.
- **DD-06: The CLI never enforces DevOps rules itself.** Ordering, concurrency, the `RUNNING` checks and the instrumented-only checks stay on the server. The CLI checks only its own input:
  - required options;
  - readable files;
  - a mappable descriptor;
  - a JSON object for results;
  - no coordinate or run id that is empty or still an unresolved `${…}` / `$VAR` reference.
- **DD-07: Failures are explicit** _(team, rev. 2)_. Every failure ends the command with exit `1`, or `2` for usage errors. The final message names the cause and the step:
  - activity rejected;
  - task rejected;
  - DevOps refusal, with its message verbatim;
  - version not found in Registry;
  - version not `APPROVED` (with the hint `--force-start`);
  - timeout;
  - service unreachable;
  - invalid input.

  Error bodies are translated inside the clients, so the message is the server's text and not `{code=…, responseBody='…'}`. Two other cases have their own message:
  - **Unreachable service:** "Could not reach the ODM Platform <service> service".
  - **Body that is not JSON:** a short summary of the body, not the body itself.

  A DevOps 409 serialization conflict is retried a bounded number of times (`conflictRetries`, 3 × `conflictRetryInterval`, 1 s). A Registry 409 is not retried.
- **DD-08: One DevOps client and configuration entry.** `cli.platform.services.devops` takes `url`, `headers` and `oauth2`, the same shape as `registry`, with `url` as the base address only. Only the devops commands read it.
- **DD-09: Every parameter is an explicit option** _(team, rev. 2)_. There are no environment-variable fallbacks and no state file.
  - **`start-task`:** `--data-product-fqn`, `--data-product-version`, `--activity`, `--task` and `--provider-run-id`, plus the optional `--force-start` flag.
  - **`complete-task`:** `--provider-run-id`, which must be the value given to `start-task` (instrumented) or the pipeline's own run id (full control). It also takes `--status` (or `--results-only`), and optional `--logs-file` (repeatable) and `--results-file`. No coordinates and no `--activity-uuid` (rev. 6).

  The pipeline maps its own variables, such as `GITHUB_RUN_ID`, onto the options. The option is named `--data-product-version`, not "tag", because Registry's `tag` is the git tag.
- **DD-10: Devops commands never prompt.** They run in CI, where `System.console()` is not available and the packaged default is `interactive: true`. A missing value fails as invalid usage.
- **DD-11: The activity plan follows the UI's rules, for the parts an instrumented task needs** _(revised in rev. 4: source is Registry)_. The plan is built from the Registry version and its descriptor `content`, as the UI does:
  - **Version uuid:** from the version.
  - **FQN:** the `--data-product-fqn` option, which matched the data product.
  - **Version:** the descriptor's `info.version`.
  - **Activity:** the stage named by `--activity`, with its position among the `lifecycleInfo` keys as sort order. The content is read as a JSON tree that keeps key order.
  - **Tasks:** that stage's entries in `order` (or position) order, with the UI's name, description, executor-name and pipeline-parameter rules, and the same blocking errors (unknown stage, no tasks, missing executor, duplicate name).

  Executor parameters are not built: an instrumented task does not need them. _Trade-off:_ the mapping exists in two places (the UI's JavaScript and the CLI's Java). Mitigation: port the UI's test cases (`ExecuteDevOpsV2ActivityFlow.test.js`) as CLI fixtures, and name BDMD-5518 as the reference.
- **DD-12: `complete-task` orders its writes and always attempts the status.** It checks every local input first, so nothing is sent when the input is wrong. It then uploads logs, then results, then the status.
  - **Results upload fails:** it records `FAILED`.
  - **Only the logs upload fails:** it records the requested status.

  It exits non-zero in both cases (Q19).

  **Exception: the run id never matches a `RUNNING` task.** Then there is no task to write a status to. The command fails at the first write, after the lookup retries (DD-17), and attempts nothing else.
- **DD-13: The CLI fetches the published descriptor from Registry** _(rev. 4; replaces the rev. 2 descriptor file)_. The version uuid cannot be avoided: it is random, it is not in the descriptor, and DevOps cannot resolve it. So a Registry lookup is needed for every new activity, and the descriptor costs one more call. Doing it in the CLI keeps the pipeline to one call with plain values. It needs no checkout of the data product repository. The descriptor is by construction the published version. See [Closed](#closed) Q16, Q17, Q22.
- **DD-14: V1 and V2 Registry calls side by side in one client** _(new)_. `PlatformRegistryClient` keeps the V1 validate call and gains the V2 lookup. Both use the same configuration entry and credentials, with separate `V1`/`V2` route constants, as `odm-platform-adapter-observer-blindata` does. There is no version flag and no second entry: each feature exists in only one API version.
  - **Lookup:** follows `odm-platform-pp-blueprint-server` (`RegistryClientImpl`, `ReconstructPublicationRequestedService:111-145`). It uses `getPage(Pageable.ofSize(2))` to detect duplicates, and filter classes holding only the fields to send, because every field becomes a query parameter.
  - **Plumbing:** keeps the CLI's own RestUtils, auth, `InternalException` translation and `MockRestServiceServer` tests. The expected URLs include the `sort` parameter, which the template always sends.
  - **No `application.yml` change.** The factory gains a check for a blank `url`. The docs state that `url` is the base address without `/api/...`.
  - **Created only when needed:** `start-task` builds the Registry client the first time it has to create an activity. A run that reuses an open activity works without any Registry configuration.

- **DD-15: The pipeline maps its steps to tasks by name** _(new, rev. 5)_. Each pipeline step that does a task's work hard-codes `--task` with that task's derived name (`name`, else `template.name`, else `task_<order>`). The pairing is a convention the pipeline author keeps. Nothing checks it in advance; DevOps refuses an unknown or out-of-order name at run time. A full-control pipeline needs no task name: DevOps chose the task and the run id identifies it (DD-17). The docs show the pairing with a descriptor stage and the matching pipeline file side by side. Whether `--task` may be omitted is Q25.
- **DD-16: Publication check and `--force-start`** _(new, rev. 6)_. When `start-task` creates the activity, it reads the version's `validationState` from the Registry lookup it already does (no extra call).
  - **Not in Registry:** fails, with or without the flag.
  - **`PENDING` or `REJECTED`:** fails, naming the state and the flag, unless `--force-start` is passed. With the flag, it logs a warning and continues.
  - **`APPROVED`:** continues.

  When an open activity is found, Registry is not called, so the check applies once per activity run, at creation. The activity was checked (or forced) when it was created, or created by the UI. `--force-start` also covers the open-activity cases of Q6. _Trade-off, accepted in rev. 7:_ one flag for two overrides. A pipeline that always passes it to run unapproved versions also replaces open activities; the docs say to use it only for manual runs.
- **DD-17: `complete-task` addresses the task by provider run id** _(new, rev. 6; needs the DevOps change)_. DevOps' Record Task Logs, Results and Status accept `providerRunId` as a third way to address a task, next to the activity uuid and the natural key. The CLI sends only the run id. The verification and the server plan are in [DevOps change: address the running task by provider run id](#devops-change-address-the-running-task-by-provider-run-id).
  - **Why for all three endpoints, not only results:** one addressing rule for the whole command. The `complete` step no longer needs the version (Q24), which was the coordinate that varies.
  - **Not found (404):** the CLI retries for a short, bounded time, then fails. This covers the full-control gap before DevOps stores the run id. The default is 5 attempts 2 s apart (`runIdLookupAttempts`, `runIdLookupInterval`). After the retries run out, the command fails at that step and makes no further writes.
  - **Ambiguous (400):** DevOps refuses, and the CLI shows the message.
  - **Task name with the run id:** DevOps accepts an optional `taskName` to narrow the match. The CLI does not send it.

### Alternatives Considered

- **Five fine-grained commands (`validate`, `start-task`, `upload-logs`, `upload-results`, `complete-task`).** Revision 1. Rejected by the team: too many commands for a pipeline author.
- **Descriptor file passed by the pipeline (`--descriptor-file`), with the pipeline calling Registry for the uuid.** Rev. 2. Rejected in rev. 4:
  - **It doesn't remove the call:** the pipeline would still need the Registry call for the uuid, written in shell for each CI vendor, with the same credentials.
  - **Two inputs that can disagree:** a local file and a uuid.
  - **No gain:** DevOps only runs published versions.

  A file could return later as an offline dry-run ("show the activity this would create").
- **DevOps resolves FQN + version into the uuid.** Rejected: a new DevOps → Registry dependency, against BDMD-5437's direction that DevOps does not know Registry.
- **Environment-variable mapping (e.g. `GITHUB_RUN_ID` → provider run id).** Set aside by the team in rev. 2. It ties the CLI to each CI vendor's variable names. It can be added later as defaults under the explicit options.
- **Build the full UI mapping, including full-control executor parameters.** Rejected. It needs repositories, tags, branches and executor secrets, and mixed activities are out of scope.
- **A separate `upload-results` command for full-control pipelines.** Rejected by the team in rev. 2. Covered by `complete-task --results-only`.
- **Coordinates for full-control pipelines** (Q12, closed in rev. 6 by DD-17). Set aside:
  - **Literal params:** the descriptor author copies the stage and task name into `configurations.params`.
  - **Built-in DevOps placeholders:** `${activity.uuid}`, `${task.name}`, … resolved into pipeline parameters. The pipeline must declare them as GitLab inputs.
  - **Reserved `odm_*` parameters:** GitLab drops undeclared inputs, and GitHub `workflow_dispatch` rejects them.
  - **Executor-added variables:** a contract change for every executor.

  The run id needs no descriptor or pipeline declaration. The pipeline already has it.
- **Keep the coordinates on `complete-task` as an alternative address.** Set aside: two ways to say the same thing, and the version is the coordinate that is hard to get in a multi-repository setup. They can be added back if a pipeline cannot keep its run id between steps.
- **One "run" command that wraps the pipeline's work.** Set aside. It would make the CLI the supervisor of the customer's build.
- **Persist the activity uuid in a local state file between steps.** Set aside: steps may run on different runners, and the natural key already identifies the activity.

## Risk & Gap Analysis

### Closed

| Id  | Answer | When |
| --- | ------ | ---- |
| Q1  | The command derives the activity with the UI's rules (DD-11), from the descriptor of the published Registry version (DD-13). | Rev. 2, amended rev. 4 |
| Q2  | Superseded: two commands, and `start-task` also starts the activity (team, rev. 2). | Rev. 2 |
| Q3  | Names: `start-task` and `complete-task` (team, rev. 2). | Rev. 2 |
| Q4  | Directly under `platform devops`, no `executor` subgroup (DD-03). Documented in `docs/cmd-platform-devops.md` (AC6). | Rev. 3 |
| Q5  | Every parameter is passed explicitly. Environment-variable mapping is a documented alternative only (DD-09). | Rev. 2 |
| Q7  | Poll every 5 s. Time out after 30 min waiting for activity approval and 10 min for task approval, all configurable by option and configuration. On timeout the command fails and writes no DevOps state. | Rev. 3 |
| Q8  | One results file holding a JSON object, validated before any call. | Rev. 3 |
| Q9  | Zero or more log files, one log row each; no size cap in this story. | Rev. 3 |
| Q10 | The DevOps version tag is the descriptor's `info.version`, which is Registry's `versionNumber`. The option is `--data-product-version`. | Rev. 2, amended rev. 4 |
| Q11 | A stage with full-control tasks is refused by DevOps; the CLI shows the message, and the docs mention mixed stages. | Rev. 3 |
| Q12 | A full-control pipeline addresses its task by its run id (`CI_PIPELINE_ID` on GitLab): `complete-task --results-only --provider-run-id …`. Needs the DevOps change (DD-17). Placeholders and literal params are set aside. | Rev. 6 |
| Q13 | Exit `1` for every command failure and `2` for usage, with a final message naming the cause and the step. | Rev. 3 |
| Q14 | `--status` takes `SUCCEEDED`, `FAILED` or `CANCELED`, case-insensitive; the pipeline maps CI values itself. | Rev. 3 |
| Q15 | No authentication design in this story. The DevOps client reuses the `headers` and `oauth2` configuration shape at no cost. Reaching DevOps from the pipeline is a deployment concern. | Rev. 4 |
| Q16 | The CLI fetches the descriptor from Registry (DD-13); the pipeline needs no descriptor. Confirmed by the team in rev. 6. | Rev. 4, confirmed rev. 6 |
| Q17 | The CLI looks the version up in Registry V2 by FQN and version number (DD-13, DD-14), only when it has to create the activity, and refuses a version that is not `APPROVED` unless `--force-start` (DD-16). | Rev. 4, amended rev. 6 |
| Q18 | `complete-task --results-only` for full-control tasks: it uploads results only, and `--status` is not allowed with it. Without the flag, `--status` is required. | Rev. 3 |
| Q19 | If the results upload fails, `complete-task` records `FAILED`. If only the logs upload fails, it records the requested status. It exits non-zero in both cases. | Rev. 3 |
| Q20 | `complete-task` takes only `--provider-run-id` as the task address (DD-09, DD-17). Superseded the rev. 4 answer (four coordinates or `--activity-uuid` with `--task`). | Rev. 4, amended rev. 6 |
| Q21 | Not applicable. `canDeployOnlyStage` restricts UI users; a pipeline with the right permissions may run any stage. No CLI guardrail. | Rev. 6 |
| Q22 | No drift: the descriptor is always the published version's (DD-13). | Rev. 4 |
| Q6  | `start-task --force-start` replaces an open activity that cannot accept the task; without it, plain reuse with a hint. One flag, shared with the publication override (DD-16). | Rev. 7 |
| Q25 | `--task` stays required on `start-task`. | Rev. 7 |
| Q24 | `--data-product-version` is always passed to `start-task`: from the checked-out descriptor (`yq`), else from a CI variable. The CLI does not derive it (no `--git-tag`, no "latest"). | Rev. 7 |
| Q23 | DPDS 1.x only, matching the UI; other versions are refused with a clear message. | Rev. 3 |

### Open questions under discussion

No questions open after rev. 7. The DevOps change below is decided and is kept here for its verification and plan.

#### DevOps change: address the running task by provider run id

Answers Q12 (team, rev. 6). This is a DevOps server change. The CLI depends on it.

**Is the run id enough to identify the task? Yes, for a `RUNNING` task, with four limits.** Verified in `odm-platform-pp-devops-server` on 2026-10-29:

| Check | Finding | Consequence |
| ----- | ------- | ----------- |
| Is it stored per task? | Yes: `activities_tasks.provider_run_id`, indexed (`activities_tasks_provider_run_id_idx`). | The lookup is one indexed query. |
| Is it unique? | No. No constraint, and old activities keep their ids. | Match only tasks in `RUNNING`. Terminal tasks of earlier runs drop out, and a new activity starts with null ids (`ExecuteActivity.prepareForExecution`). |
| Instrumented: is it set before the pipeline needs it? | Yes. Request Task Execution stores it while the task is `PENDING`. The task becomes `RUNNING` only after approval, and `complete-task` runs after that. | No timing gap. |
| Full control: is it set before the pipeline needs it? | Not always. `ExecuteTask` marks the task `RUNNING`, calls the executor, and stores the id in a separate transaction after the call returns. A very fast pipeline could call first. | The CLI retries a not-found for a few seconds (DD-17). The gap is short: GitLab returns the pipeline id when it creates the pipeline, and the job then needs a runner. |
| Full control: does the pipeline know the id? | Yes, for GitLab. The executor returns the created pipeline's id, which is `CI_PIPELINE_ID` inside it (`TaskStartResultRes`: "GitLab pipeline id"). | Pass `$CI_PIPELINE_ID`. In a child or downstream pipeline `CI_PIPELINE_ID` is different, so the parent's id must be passed down. This goes in the docs. Other executors must document which variable equals their run id. |

**Where it can still be ambiguous (limit 1).** Two `RUNNING` tasks with the same id:
- **Parallel jobs of one CI run sharing a pipeline-level id.** For example, two instrumented jobs that both pass `CI_PIPELINE_ID` or `GITHUB_RUN_ID`. That is why the docs use an id that is unique per job for instrumented tasks (`$CI_JOB_ID`, `github.run_id-github.job`).
- **One CI run working on two activities at once.** For example, a monorepo pipeline that runs tasks of two data products in parallel jobs, with the same `CI_PIPELINE_ID`. Within one activity, DevOps allows only one running task, so the same activity cannot cause it.
- **Two CI systems whose ids collide.** For example, two GitLab instances. This is unlikely, and only a problem if both tasks are `RUNNING` at the same moment.

DevOps refuses an ambiguous id with 400 and names the matching tasks. No write goes to the wrong task, except in a narrower case (limit 2).

**Limit 2: a collision during the full-control gap.** Task A's id is not stored yet, and another system's `RUNNING` task B has the same id. A's results would go to B. This needs a cross-system id collision inside a gap of milliseconds to seconds. It is accepted. An optional `taskName` on the request narrows the match for pipelines that know it.

**Limit 3: a re-run of the same CI run.** For example, GitHub "re-run jobs" keeps `GITHUB_RUN_ID`, and GitLab job retry keeps `CI_PIPELINE_ID`. The re-run's `start-task` already finds the earlier attempt's task `RUNNING` and is refused (Q6 case 5). So two attempts never both own a running task under the same id.

**Limit 4: authorization.** Through the Agent proxy, the record endpoints need `DATAOPS_ADMIN` and no data product, so the run id changes nothing today. A later team-scoped rule in the proxy would have to resolve the run id to the activity first.

**Do logs and status make sense too? Yes.** Both are instrumented-only, and an instrumented task's id is set before it is `RUNNING`, so they have none of the full-control timing gap. Benefits:
- one address for the whole `complete-task`;
- the `complete` step needs no version, FQN, activity or task name, so the multi-repository problem (Q24) is limited to `start-task`.

The mode checks stay: logs and status still refuse full-control tasks.

**Decision:** yes, for all three endpoints. The existing addresses stay for other callers.

**Server plan (`odm-platform-pp-devops-server`).** Implemented as planned, REASONS `spdd/prompt/BDMD-5443-202610291208-[Feat]-api-task-address-by-provider-run-id.md` in that repository. The unit tests pass. The integration tests (`ActivityInstrumentedFlowIT`) have not run yet because they need Docker.
1. **Resources:** add `providerRunId` to `ActivityTaskLogsCommandRes`, `ActivityTaskResultsCommandRes` and `ActivityTaskStatusCommandRes`, and to the matching `Record*Command` records and mappers.
2. **Validation, in each record use case:** exactly one address is allowed: activity uuid (results only), natural key, or `providerRunId`. `taskName` is required with the first two and optional with the run id, where it narrows the match. Mixed addresses are a 400.
3. **Lookup:** a persistence-port method `findActivitiesWithRunningTask(providerRunId[, taskName])`, backed by a query on tasks with `provider_run_id = ? and status = RUNNING`, using the existing index. The use case then:
   - **No match:** 404 "No RUNNING task with provider run id X".
   - **More than one:** 400 "Provider run id X matches more than one RUNNING task: …", naming them.
   - **One match:** loads the activity with `findForUpdate` and re-checks the task inside the transaction, as today. The `requireRunning`, `requireInstrumented` and advance-activity steps are unchanged.
4. **Tests:**
   - the use cases: each address, ambiguous, not found, and terminal tasks with the same id ignored;
   - one web test per endpoint;
   - a full-control test where results arrive by run id.
5. **Docs:** the OpenAPI descriptions, and the BDMD-5442 instrumented-path docs.

The executor needs no change, and there is no migration (the index exists). Estimate: small, the same shape in three use cases.

#### Q6: reusing an open activity (confirmed in rev. 7: covered by `--force-start`)

Reuse is required: in a multi-task pipeline, every task after the first finds the activity open. DevOps refuses a bad request without changing anything, so the risk is not corruption. The risk is an activity left open, which blocks every later run of the same version and stage, because only one open activity is allowed.

| #   | Open activity found | Typical cause | Outcome with plain reuse | Acceptable? |
| --- | ------------------- | ------------- | ------------------------ | ----------- |
| 1   | `PENDING` (awaiting approval) | A concurrent run started it | `start-task` waits for approval, then requests. | Yes |
| 2   | `RUNNING`, the requested task is the next `PENDING` one, nothing else running | Normal multi-task flow | Requested and started. | Yes |
| 3   | `RUNNING`, the requested task already `SUCCEEDED`, a later one `PENDING` | Pipeline re-run from the start after a crash or cancel past task 1; or a second trigger of the same pipeline | Refused ("not PENDING"); the step fails. The activity stays open waiting for task 2, and a re-run from the start fails again on task 1. | Failing is right, but the activity stays blocked until the remaining tasks run or someone closes it. |
| 4   | `RUNNING`, the requested task `RUNNING` | The previous run died between `start-task` and `complete-task` | Refused ("not PENDING"); the step fails. **Cancel from the UI does not close it**: it waits for an instrumented status that never comes, then errors. Only Record Task Status closes it. | Not acceptable long term without a recovery path. |
| 5   | `RUNNING`, the requested task `RUNNING` with the **same** provider run id | The same CI run re-attempted (e.g. GitHub "re-run jobs" keeps `GITHUB_RUN_ID`) | Refused ("not PENDING"). | Could be a resume, but the earlier attempt's half-done work is not visible. |
| 6   | `RUNNING`, the requested task `PENDING` but not next, or another task `RUNNING` | Steps out of order, or a parallel run | Refused with DevOps' message. | Yes |
| 7   | `RUNNING`, no task with that name | The open activity was built from another descriptor or by the UI | Refused ("not found"). | Yes |
| 8   | Activity terminal | Earlier run finished | Not matched as open; a new activity is created. | Yes |

Cases 3–5 need an abnormal end of a pipeline: a lost runner, a killed job, or a `complete-task` step that is not set to always run.

**Decision (rev. 6, confirmed rev. 7):** `--force-start` covers these cases, with one flag only:
- **Without the flag:** plain reuse, as above. A refusal in cases 3–5 is printed together with the hint to re-run with `--force-start`.
- **With the flag,** when the open activity cannot accept the requested task (cases 3–5, and 6 when the blocker is a stale `RUNNING` task), `start-task` replaces it:
  1. Record `CANCELED` on each `RUNNING` instrumented task of that activity, by natural key and task name. This is the only call that closes such a task. DevOps then ends the activity and cancels its pending tasks.
  2. If the activity is still open (no running task, case 3), call Cancel Activity.
  3. Wait until it is terminal, then continue as when no activity was open: Registry lookup, publication check, Execute, and so on.
- **Cases 1, 2, 7, 8 are unchanged** with the flag. Case 7 still fails, because replacing an activity created for different tasks is not safe to do blindly.

**Risk:** the flag closes the activity of a run that is still alive (a concurrent trigger, or a run that is slow but not dead). The CLI cannot tell a dead run from a live one. That is why the flag is an operator override, used for a manual re-run, and not set in pipeline files by default. The docs say so. The team kept one flag for both overrides (rev. 7).

**Server follow-up, unchanged:** a UI cancel that can close an abandoned instrumented task (for example after a grace period). The CLI flag does not help a UI user.

#### Q24: where `start-task` gets the version (closed in rev. 7)

Only `start-task` needs `--data-product-version` and `--activity`; `complete-task` uses the run id (DD-17). FQN and task name are static in the pipeline file (DD-15). The activity (stage) is static when the pipeline serves one stage; otherwise the pipeline's own logic chooses it, for example by branch or environment.

**Decided (rev. 7):** the version comes from the descriptor in the checkout when it is there (`yq '.info.version' <descriptor>`). Otherwise it is a CI variable set by whoever triggers the pipeline. Both are documented with GitHub Actions and GitLab examples. The value must equal Registry's `versionNumber`.

**Rejected (rev. 7): resolving the version in the CLI.** The CLI only looks the version up; it never works out which version it is. Two options were set aside:
- **`--git-tag`:** Registry lookup by the release's git tag, including the additional repositories' tags. It needs a Registry filter for the additional repositories and works only in tag pipelines.
- **The latest `APPROVED` version:** ambiguous for hotfixes on an older release, and unstable if someone publishes during the run.

#### Q25: may `--task` be omitted? (closed in rev. 7: no)

No omission for now: `--task` stays required on `start-task`. The reasons: a step could silently do another task's work, `start-task` would have to print the name for the pipeline to capture, and the name is the step's natural label. It can be revisited later.

### Edge Cases

- **Activity rejected by policy.** `start-task` sees the activity `FAILED` and fails, naming the rejection. No task is requested.
- **Task rejected by policy after the request.** `start-task` sees the task `FAILED` and fails. DevOps fails the activity and cancels the pending tasks. `complete-task` must not run: the pipeline runs it only if `start-task` succeeded, or it fails with "not RUNNING".
- **Task not next, or another task `RUNNING`.** DevOps refuses Request Task Execution with a 400. `start-task` prints the message verbatim and fails (AC4). The activity may already have been created; a later run finds and reuses it.
- **Open activity in an unexpected state** (task already `SUCCEEDED` or still `RUNNING` from an abandoned run). Refused, with the `--force-start` hint; with the flag, the activity is replaced (Q6).
- **Version in Registry but `PENDING` or `REJECTED`.** `start-task` fails before touching DevOps, naming the state; with `--force-start` it warns and continues (DD-16).
- **Work failed in the pipeline.** `complete-task` runs with `--status FAILED`. Otherwise the task stays `RUNNING` and blocks later runs.
- **Activity canceled by a user while the task runs.** DevOps cancels the pending tasks and waits for `complete-task`. The activity ends `CANCELED` whatever status is sent.
- **Data product or version not found in Registry** (wrong FQN, wrong version, or the descriptor's version was never published). `start-task` fails before touching DevOps, naming FQN and version. If an open activity exists, Registry is not called and this cannot happen.
- **More than one product or version matches in Registry.** `start-task` fails with a clear message rather than picking one (`getPage` of size 2).
- **Unknown stage, empty stage, missing executor reference, or duplicate task names in the descriptor.** `start-task` fails before Execute Activity, with the same causes the UI reports.
- **`--task` that does not match a derived name.** `start-task` fails before Execute Activity and lists the stage's task names.
- **The open activity was created by the UI** (same version and stage). `start-task` reuses it; if its tasks are full-control, DevOps refuses the request.
- **Version deleted and recreated in Registry while an old activity is open.** The natural key matches two open activities, and DevOps refuses with "More than one open activity" (server-side gap).
- **Full-control `complete-task --results-only` after the executor reported its outcome.** Refused (task not `RUNNING`). The pipeline must upload before its run ends.
- **Run id not found yet** (full control, before DevOps stores it). `complete-task` retries briefly, then fails naming the id, without further writes (DD-17).
- **Run id matches several `RUNNING` tasks** (one CI run working on two activities at once). DevOps refuses with the matching tasks; the CLI shows the message.
- **Full-control work in a child or downstream pipeline.** Its `CI_PIPELINE_ID` is not the task's run id; the parent's id must be passed down, or the lookup finds nothing.
- **`complete-task` with a run id other than the one given to `start-task`.** Not found, or another task's. The docs show both steps using the same variable.
- **Empty or unresolved run id** (`$CI_PIPELINE_ID` not expanded). `complete-task` fails as invalid usage before any call.
- **Large logs or results.** No server limit; they may hit gateway limits.

### Technical Risks

- **Interactive default in CI.** `interactive: true` is the packaged default, and `System.console()` is `null` in CI. Any prompt fails with an NPE instead of a clear message. Mitigation: DD-10.
- **Error messages.** `ClientException.getMessage()` exposes the raw response body. Without parsing the DevOps error body, AC4 ("mostrato in chiaro") is not met. Mitigation: DD-07.
- **Long-running, multi-call commands.** `start-task` can wait tens of minutes and calls two services. A failure in the middle leaves state behind: an activity created but no task requested. Mitigation: every step is idempotent or reusable, and the final message names the step that failed.
- **Abandoned instrumented tasks cannot be canceled** (Q6). Mitigation in this story: `start-task --force-start` replaces the blocked activity, or `complete-task --provider-run-id <the dead run's id> --status CANCELED` closes the stuck task. A UI user still needs the DevOps follow-up.
- **Two copies of the mapping** (UI JavaScript, CLI Java). They will drift when BDMD-5518 changes. Mitigation: port the UI's test cases as shared fixtures.
- **Descriptor key order.** The stage sort order depends on the key order of `lifecycleInfo`. The content must be read as a JSON tree that keeps order (Jackson `ObjectNode` does); no mapping to a sorted map.
- **Registry V2 API dependency.** The CLI now depends on three V2 endpoints, plus the V1 validate endpoint. Mitigation: route constants and `MockRestServiceServer` tests for each URL (DD-14).
- **Dependency on a DevOps change** (DD-17). `complete-task` cannot work against a DevOps without run-id addressing. Mitigation: ship the server change first; the server is not deployed yet.
- **Run id is not unique** (DD-17). Mitigated by matching only `RUNNING` tasks and refusing ambiguity; the residual cross-system collision during the full-control gap is accepted.
- **`--force-start` closes a live run** (Q6). Mitigation: documented as an operator override, not a pipeline default; the refusal names the activity's task statuses.
- **Configuration in containers.** Simple keys bind from the environment (verified by the Docker analysis for `CLI_PLATFORM_SERVICES_REGISTRY_URL`), so `CLI_PLATFORM_SERVICES_DEVOPS_URL` will too. List keys (`headers`) and nested camelCase keys (`oauth2.clientId`) are still unverified.
- **409 conflicts.** Concurrent writes to the same activity (full-control results racing the executor outcome) can return 409. Mitigation: bounded retry in the client.
- **DevOps integration tests not yet run.** The run-id query and the new integration tests compiled, and the local end-to-end tests exercised them through the real database. The integration test suite still needs a Docker host before merging.
- **Noisy failure output.** On failure the CLI prints a full Java stack trace after the error line. The raw response body is no longer in it, but the trace clutters pipeline logs. Possible follow-up.
- **DevOps contract drift.** The server is new and its analyses already differ from the code. Mitigation: the canvas takes field names from the code (`odm-platform-pp-devops-server` `rest/v2/resources`).

### Acceptance Criteria Coverage

| AC# | Description | Addressable? | Gaps/Notes |
| --- | ----------- | ------------ | ---------- |
| 1   | Esecuzione activity + polling fino a running/failed | Yes | Inside `start-task` (DD-04, DD-05, DD-11, DD-13). |
| 2   | Request task con nome + provider run ID; polling fino a running/failed | Yes | Inside `start-task`. |
| 3   | Ordine upload: log → risultati → stato | Yes | Enforced within `complete-task` (DD-12). |
| 4   | Errore DevOps su ordinamento/concorrenza mostrato in chiaro e exit code ≠ 0 | Yes | DD-07. Needs the client-side error translation. |
| 5   | Comandi sotto platform devops: validate con polling, upload result, upload logs, comando finale di stato con nome definito | Yes, as revised | The team replaced the four commands with `start-task` (covers validate) and `complete-task` (covers upload result, upload logs and the final status). The AC text should be updated to match. |
| 6   | Deciso e documentato il posizionamento dei comandi | Yes | Directly under `platform devops` (DD-03), documented in `docs/cmd-platform-devops.md`. |
| 7   | Immagine Docker della CLI utilizzabile in pipeline | No (separate analysis) | `BDMD-5443-202610270941-[Analysis]-cli-docker-image.md`. |
| —   | Implicit: results upload usable from a full-control pipeline | Yes, with the DevOps change | `complete-task --results-only --provider-run-id` (Q12, Q18, DD-17), shared pipelines included. |
| —   | Implicit: the pipeline can supply every coordinate | Yes, documented | Only `start-task` needs them: Q24 recipes; step-to-task pairing by name (DD-15). `complete-task` needs only the run id. |
| —   | Implicit (rev. 6): unpublished versions do not run by accident | Yes | DD-16, `--force-start` to override. |
| —   | Implicit: abandoned activities can be recovered | Yes, from the CLI | Q6: `--force-start`, or `complete-task --status CANCELED` with the dead run's id; DevOps cancel follow-up for the UI. |

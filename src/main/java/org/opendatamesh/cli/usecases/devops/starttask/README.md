# Unit starttask

Brings one instrumented task to RUNNING. Entry class: `StartTask`, built by `StartTaskFactory` from a `StartTaskCommand` and a `StartTaskPresenter`.

## Input and output

- **In:** the data product FQN, the version number (the descriptor's info.version), the activity (stage) name, the task name, the pipeline's run id, `forceStart`, the poll interval and the two approval timeouts.
- **Out:** `presentTaskStarted` when the activity and the task are RUNNING. Any failure goes to `presentFailedStep` with a `StartTaskStep`, then to the presenter's exception handling.

## Procedure

Obtain the activity, await the activity approval, request the task, await the task approval, present. The index README has the canonical script.

## Cases handled

- **No open activity:** the version is looked up in Registry, the activity is planned from the descriptor and executed.
- **Open activity:** it is reused and Registry is not called.
- **Open activity that blocks the task:** without `forceStart` a hint is presented and DevOps refuses the request. With `forceStart` the activity is closed and a new one is created.
- **Concurrent creation:** DevOps answers that the activity is already open, so the open activity is searched again and used. The refusal is recognised by `StartTaskDevOpsOutboundPortImpl`.
- **Rejection or timeout of an approval:** the command fails and writes nothing to DevOps.

## Step classes and their rules

- `OpenActivity` finds the open activity and replaces it. Rules: `blocked`, `forceStartHint`, `replace`, `moreThanOneOpen`.
- `VersionLookup` finds the published version in Registry. Rules: `productNotFound`, `productAmbiguous`, `versionNotFound`, `versionAmbiguous`, `notApproved`, `noDescriptorContent`. A version missing from Registry fails even with `forceStart`.
- `ActivityPlan` turns the descriptor stage into the activity to execute, as the UI does (BDMD-5518). Rules: `unsupportedDescriptor`, `unknownStage`, `noTasks`, `missingExecutor`, `duplicateTaskName`, `unknownTask`.
- `Approval` waits for the activity and for the task. Rules: `activityRejected`, `activityEnded`, `taskRejected`, `taskCanceled`, `taskEnded`, `activityTimedOut`, `taskTimedOut`.

## Ports

`StartTaskDevOpsOutboundPort`, `StartTaskRegistryOutboundPort`, `StartTaskDescriptorOutboundPort` and `StartTaskClockOutboundPort`, with their `*Impl` classes. Types owned by the unit: `DescriptorLifecycle`, `DescriptorTask` and `ActivityCreation`.

## Tests that prove it

- `StartTaskTest`: one test per scenario of the story.
- `ActivityPlanTest`: the plan rules, with the UI's fixtures.
- `StartTaskDescriptorOutboundPortImplTest`: key order, executor fallback, params.
- `StartTaskDevOpsOutboundPortImplTest`: the race detection.
- `PlatformDevOpsCommandIT`: the command end to end.

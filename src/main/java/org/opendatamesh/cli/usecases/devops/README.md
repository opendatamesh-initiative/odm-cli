# Module usecases/devops

The use cases behind `odm-cli platform devops`. They let a CI pipeline run an instrumented task of an ODM Platform DevOps V2 activity under platform governance. Story BDMD-5443.

## Reading order

1. This index: which unit does what, and the procedure each unit follows.
2. The README of the unit you need ([starttask](starttask/README.md) or [completetask](completetask/README.md)): the cases it handles and the rules by name.
3. The unit's entry class, then its step classes, then the rules at the bottom of each step class.
4. The `*OutboundPortImpl` classes and the clients in `clients/platform/{devops,registry}` hold the I/O. They decide nothing.

## Units

| Unit | Command | Answers |
| --- | --- | --- |
| [starttask](starttask/README.md) | `start-task` | How does one instrumented task reach RUNNING: find or create the activity, wait for approvals, request the task. |
| [completetask](completetask/README.md) | `complete-task` | How is a running task closed (logs, results, status), or how does a full-control task upload its results, addressed by its run id. |

The units never depend on each other. They share no types: the client domain types of `clients/platform/devops` and `clients/platform/registry` are the only common vocabulary.

## Canonical procedures

- **start-task:** obtain the activity, await the activity approval, request the task, await the task approval, present.
- **complete-task:** check the input, then either (results only) upload the results, or (instrumented) upload the logs, upload the results, record the status, then present.

The entry method of each unit is that script and nothing else. Failures never leave the entry method: they go to the presenter.

## Tests that prove the module

`DevOpsUseCasesStructureTest` checks the layering and the independence of the units. `DevOpsUseCasesReadmeGuardTest` checks the READMEs.

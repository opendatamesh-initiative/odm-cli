package org.opendatamesh.cli.usecases.devops.starttask;

/** The steps of start-task, named in its progress and failure messages. */
public enum StartTaskStep {
    FIND_ACTIVITY,
    REPLACE_ACTIVITY,
    LOOK_UP_VERSION,
    PLAN_ACTIVITY,
    EXECUTE_ACTIVITY,
    AWAIT_ACTIVITY_APPROVAL,
    REQUEST_TASK,
    AWAIT_TASK_APPROVAL
}

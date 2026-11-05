package org.opendatamesh.cli.usecases.devops.completetask;

/** The outcome of one write to the running task: recorded, or DevOps has no RUNNING task with the run id yet. */
enum TaskWrite {
    RECORDED,
    NO_RUNNING_TASK
}

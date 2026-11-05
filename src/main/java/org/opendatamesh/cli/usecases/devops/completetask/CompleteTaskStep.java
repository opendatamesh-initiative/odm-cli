package org.opendatamesh.cli.usecases.devops.completetask;

/** The steps of complete-task, named in its progress and failure messages. */
public enum CompleteTaskStep {
    CHECK_INPUT,
    UPLOAD_LOGS,
    UPLOAD_RESULTS,
    RECORD_STATUS
}

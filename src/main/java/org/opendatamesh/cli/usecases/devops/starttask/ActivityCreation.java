package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;

/** The outcome of Execute Activity: the new activity, or DevOps refusing because one is already open. */
sealed interface ActivityCreation {

    record Created(DevOpsActivity activity) implements ActivityCreation {
    }

    record AlreadyOpen() implements ActivityCreation {
    }
}

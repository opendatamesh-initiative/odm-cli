package org.opendatamesh.cli.clients.platform.devops;

/** Natural key of an activity: data product FQN, version number (the descriptor's info.version) and activity name. */
public record DevOpsActivityKey(String dataProductFqn, String dataProductVersionNumber, String activityName) {
}

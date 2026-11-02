package org.opendatamesh.cli.clients.platform.devops;

/** Natural key of an activity: data product FQN, version tag (the descriptor's info.version) and activity name. */
public record DevOpsActivityKey(String dataProductFqn, String dataProductVersionTag, String activityName) {
}

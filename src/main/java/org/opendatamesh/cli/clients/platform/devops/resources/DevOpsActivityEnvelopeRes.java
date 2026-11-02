package org.opendatamesh.cli.clients.platform.devops.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of Execute Activity and Cancel Activity, and their responses: the activity under {@code activity}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DevOpsActivityEnvelopeRes(DevOpsActivityRes activity) {
}

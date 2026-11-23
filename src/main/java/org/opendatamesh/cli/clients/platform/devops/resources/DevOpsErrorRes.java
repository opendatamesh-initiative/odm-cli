package org.opendatamesh.cli.clients.platform.devops.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The platform error body. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DevOpsErrorRes(Integer status, String error, String message, String path) {
}

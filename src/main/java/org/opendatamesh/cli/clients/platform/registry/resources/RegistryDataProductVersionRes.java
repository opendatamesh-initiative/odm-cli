package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistryDataProductVersionRes(String uuid, String versionNumber, String validationState, JsonNode content) {
}

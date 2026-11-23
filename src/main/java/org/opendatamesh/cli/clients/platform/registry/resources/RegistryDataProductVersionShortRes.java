package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistryDataProductVersionShortRes(String uuid, String dataProductUuid, String versionNumber, String validationState) {
}

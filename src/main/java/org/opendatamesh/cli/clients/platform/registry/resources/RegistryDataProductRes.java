package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistryDataProductRes(String uuid, String fqn) {
}

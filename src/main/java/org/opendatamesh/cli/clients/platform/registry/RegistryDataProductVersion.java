package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;

public record RegistryDataProductVersion(
        String uuid,
        String versionNumber,
        RegistryValidationState validationState,
        JsonNode content
) {
}

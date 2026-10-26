package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;

public interface PlatformRegistryClient {
    DataProductValidationResults validateDataProduct(JsonNode dataProductDescriptor);
}

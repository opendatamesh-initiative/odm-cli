package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface PlatformRegistryClient {
    /**
     * @param policyEvents the policy evaluation events to validate against (e.g. DATA_PRODUCT_CREATION); all when empty
     */
    DataProductValidationResults validateDataProduct(JsonNode dataProductDescriptor, List<String> policyEvents);
}

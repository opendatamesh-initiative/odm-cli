package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface PlatformRegistryClient {
    /**
     * Registry V1: {@code POST /api/v1/pp/registry/validate/report}.
     *
     * @param policyEvents the policy evaluation events to validate against (e.g. DATA_PRODUCT_CREATION); all when empty
     */
    DataProductValidationResults validateDataProduct(JsonNode dataProductDescriptor, List<String> policyEvents);

    /**
     * Registry V2: {@code GET /api/v2/pp/registry/products?fqn=&page=0&size=2&sort=}.
     *
     * @return at most 2 products, so the caller can detect a duplicate
     */
    List<RegistryDataProduct> findDataProductsByFqn(String fqn);

    /**
     * Registry V2: {@code GET /api/v2/pp/registry/products-versions?dataProductUuid=&versionNumber=&page=0&size=2&sort=}.
     *
     * @return at most 2 versions, so the caller can detect a duplicate
     */
    List<RegistryDataProductVersionSummary> findDataProductVersions(String dataProductUuid, String versionNumber);

    /**
     * Registry V2: {@code GET /api/v2/pp/registry/products-versions/{uuid}}.
     */
    RegistryDataProductVersion getDataProductVersion(String uuid);
}

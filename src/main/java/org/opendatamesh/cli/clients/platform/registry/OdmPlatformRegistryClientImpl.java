package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;
import org.opendatamesh.cli.clients.utils.ClientException;
import org.opendatamesh.cli.clients.utils.ClientResourceMappingException;
import org.opendatamesh.cli.clients.utils.RestUtils;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.web.client.RestTemplate;

class OdmPlatformRegistryClientImpl implements OdmPlatformRegistryClient {

    private final RestUtils restUtils;
    private final OdmCliConfiguration.OdmPlatformService registryServiceConfig;

    OdmPlatformRegistryClientImpl(
            RestTemplate restTemplate,
            OdmCliConfiguration.OdmPlatformService registryService
    ) {
        this.restUtils = new RestUtils(restTemplate);
        this.registryServiceConfig = registryService;
    }

    @Override
    public OdmDataProductValidationResponseResource validateDataProduct(OdmDataProductValidationRequestResource validationRequest) {
        try {
            return restUtils.genericPost(
                    registryServiceConfig.getEndpoint() + "/api/v1/pp/registry/products/*/validate",
                    null,
                    validationRequest,
                    OdmDataProductValidationResponseResource.class
            );
        } catch (ClientException | ClientResourceMappingException e) {
            throw new RuntimeException(e);
        }
    }
}

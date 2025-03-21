package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;
import org.opendatamesh.cli.clients.utils.BaseAuthenticatedRestUtils;
import org.opendatamesh.cli.clients.utils.ClientException;
import org.opendatamesh.cli.clients.utils.ClientResourceMappingException;
import org.opendatamesh.cli.clients.utils.RestUtils;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.springframework.web.client.RestTemplate;

class PlatformRegistryClientImpl implements PlatformRegistryClient {

    private final RestUtils restUtils;
    private final PlatformServiceCredentials registryServiceCredential;

    PlatformRegistryClientImpl(
            RestTemplate restTemplate,
            PlatformServiceCredentials registryService
    ) {
        this.restUtils = new BaseAuthenticatedRestUtils(
                restTemplate,
                registryService.getHeaders(),
                registryService.getOauth2()
        );
        this.registryServiceCredential = registryService;
    }

    @Override
    public OdmDataProductValidationResponseResource validateDataProduct(OdmDataProductValidationRequestResource validationRequest) {
        try {
            return restUtils.genericPost(
                    registryServiceCredential.getUrl() + "/api/v1/pp/registry/products/*/validate",
                    null,
                    validationRequest,
                    OdmDataProductValidationResponseResource.class
            );
        } catch (ClientException | ClientResourceMappingException e) {
            throw new RuntimeException(e);
        }
    }
}

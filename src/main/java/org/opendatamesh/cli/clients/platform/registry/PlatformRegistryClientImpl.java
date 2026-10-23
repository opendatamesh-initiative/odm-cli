package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.client.ClientException;
import org.opendatamesh.cli.exceptions.client.ClientResourceMappingException;
import org.opendatamesh.cli.utils.client.RestUtils;
import org.opendatamesh.cli.utils.client.RestUtilsFactory;
import org.springframework.web.client.RestTemplate;

class PlatformRegistryClientImpl implements PlatformRegistryClient {

    private final RestUtils restUtils;
    private final PlatformServiceCredentials registryServiceCredential;

    PlatformRegistryClientImpl(
            RestTemplate restTemplate,
            PlatformServiceCredentials registryService
    ) {
        this.restUtils = RestUtilsFactory.getAuthenticatedRestUtils(
                restTemplate,
                registryService.getHeaders(),
                registryService.getOauth2()
        );
        this.registryServiceCredential = registryService;
    }

    @Override
    public OdmDataProductValidationResponseResource testValidateDataProduct(OdmDataProductValidationRequestResource validationRequest) {
        try {
            return restUtils.genericPost(
                    registryServiceCredential.getUrl() + "/api/v1/pp/registry/validate/report",
                    null,
                    validationRequest,
                    OdmDataProductValidationResponseResource.class
            );
        } catch (ClientException e) {
            throw new InternalException("ODM Platform registry service failed to validate the data product: " + e.getMessage(), e);
        } catch (ClientResourceMappingException e) {
            throw new InternalException("Unable to read the ODM Platform registry service validation response: " + e.getMessage(), e);
        }
    }
}

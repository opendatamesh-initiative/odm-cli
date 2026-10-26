package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.client.ClientException;
import org.opendatamesh.cli.exceptions.client.ClientResourceMappingException;
import org.opendatamesh.cli.utils.client.RestUtils;
import org.opendatamesh.cli.utils.client.RestUtilsFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
    public DataProductValidationResults validateDataProduct(JsonNode dataProductDescriptor, List<String> policyEvents) {
        OdmDataProductValidationResponseResource response = postValidationReport(toValidationRequest(dataProductDescriptor, policyEvents));
        return toValidationResults(response);
    }

    private OdmDataProductValidationResponseResource postValidationReport(OdmDataProductValidationRequestResource validationRequest) {
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

    private OdmDataProductValidationRequestResource toValidationRequest(JsonNode dataProductDescriptor, List<String> policyEvents) {
        OdmDataProductValidationRequestResource validationRequest = new OdmDataProductValidationRequestResource();
        validationRequest.setValidatePolicies(true);
        validationRequest.setValidateSyntax(true);
        validationRequest.setPolicyEventTypes(new ArrayList<>(policyEvents));
        validationRequest.setDataProductVersion(dataProductDescriptor);
        return validationRequest;
    }

    private DataProductValidationResults toValidationResults(OdmDataProductValidationResponseResource response) {
        DataProductValidationResults results = new DataProductValidationResults();
        results.getResults().add(toValidationResult("Data Product Descriptor Syntax", response.getSyntaxValidationResult()));
        response.getPoliciesValidationResults()
                .forEach((policyName, output) -> results.getResults().add(toValidationResult(policyName, output)));
        return results;
    }

    private DataProductValidationResults.Result toValidationResult(String name, OdmDataProductValidationResponseResource.OdmDataProductValidationResult output) {
        return new DataProductValidationResults.Result(
                name,
                output.isValidated(),
                output.getValidationOutput(),
                output.getBlockingFlag()
        );
    }
}

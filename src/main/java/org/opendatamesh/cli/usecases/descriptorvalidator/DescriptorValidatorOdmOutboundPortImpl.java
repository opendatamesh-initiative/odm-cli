package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.clients.platform.registry.PlatformRegistryClient;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;

import java.util.List;

class DescriptorValidatorOdmOutboundPortImpl implements DescriptorValidatorOdmOutboundPort {
    private final PlatformRegistryClient registryClient;

    DescriptorValidatorOdmOutboundPortImpl(PlatformRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public DataProductValidationResults validateDataProduct(JsonNode rawDescriptor, List<String> policyEvents) {
        OdmDataProductValidationRequestResource validationRequest = new OdmDataProductValidationRequestResource();
        validationRequest.setValidatePolicies(true);
        validationRequest.setValidateSyntax(true);
        validationRequest.setDataProductVersion(rawDescriptor);

        OdmDataProductValidationResponseResource response = registryClient.testValidateDataProduct(validationRequest);

        DataProductValidationResults results = new DataProductValidationResults();
        results.getResults().add(new DataProductValidationResults.Result(
                "Data Product Descriptor Syntax",
                response.getSyntaxValidationResult().isValidated(),
                response.getSyntaxValidationResult().getValidationOutput(),
                response.getSyntaxValidationResult().getBlockingFlag()
        ));
        response.getPoliciesValidationResults()
                .forEach((policyName, output) -> results.getResults().add(new DataProductValidationResults.Result(
                        policyName,
                        output.isValidated(),
                        output.getValidationOutput(),
                        output.getBlockingFlag()
                )));
        return results;
    }

}

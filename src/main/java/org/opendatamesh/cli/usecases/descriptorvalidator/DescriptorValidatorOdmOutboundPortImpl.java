package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.clients.platform.registry.DataProductValidationResults;
import org.opendatamesh.cli.clients.platform.registry.PlatformRegistryClient;

import java.util.List;

class DescriptorValidatorOdmOutboundPortImpl implements DescriptorValidatorOdmOutboundPort {
    private final PlatformRegistryClient registryClient;

    DescriptorValidatorOdmOutboundPortImpl(PlatformRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public DataProductValidationResults validateDataProduct(JsonNode rawDescriptor, List<String> policyEvents) {
        //The policy events are not sent to the registry service yet: the descriptor is validated against all the policies
        return registryClient.validateDataProduct(rawDescriptor);
    }

}

package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.clients.platform.registry.DataProductValidationResults;

import java.util.List;

interface DescriptorValidatorOdmOutboundPort {
    DataProductValidationResults validateDataProduct(JsonNode rawDescriptor, List<String> policyEvents);
}

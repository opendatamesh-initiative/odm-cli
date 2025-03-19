package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

interface DescriptorValidatorOdmOutboundPort {
    DataProductValidationResults validateDataProduct(JsonNode rawDescriptor, List<String> policyEvents);
}

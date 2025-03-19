package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;

interface DescriptorValidatorParserOutboundPort {
    JsonNode getCanonicalRawDescriptor();
}

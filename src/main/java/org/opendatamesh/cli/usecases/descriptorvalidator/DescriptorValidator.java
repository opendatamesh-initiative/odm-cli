package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.usecases.UseCaseReturning;

import java.util.List;

class DescriptorValidator implements UseCaseReturning<DataProductValidationResults> {

    private final DescriptorValidatorOdmOutboundPort odmOutboundPort;
    private final DescriptorValidatorParserOutboundPort parserOutboundPort;
    private final DescriptorValidatorParametersOutboundPort parametersOutboundPort;

    DescriptorValidator(DescriptorValidatorOdmOutboundPort odmOutboundPort, DescriptorValidatorParserOutboundPort parserOutboundPort, DescriptorValidatorParametersOutboundPort parametersOutboundPort) {
        this.odmOutboundPort = odmOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.parametersOutboundPort = parametersOutboundPort;
    }

    @Override
    public DataProductValidationResults execute() {
        JsonNode dataProductDescriptor = parserOutboundPort.getCanonicalRawDescriptor();
        List<String> policyEvents = parametersOutboundPort.getPolicyEvents();
        return odmOutboundPort.validateDataProduct(dataProductDescriptor, policyEvents);
    }

}

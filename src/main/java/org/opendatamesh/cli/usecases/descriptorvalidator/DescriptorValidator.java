package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.utils.usecases.UseCase;

import java.util.List;

class DescriptorValidator implements UseCase {

    private final DescriptorValidatorOdmOutboundPort odmOutboundPort;
    private final DescriptorValidatorParserOutboundPort parserOutboundPort;
    private final DescriptorValidatorParametersOutboundPort parametersOutboundPort;
    private final DescriptorValidatorPresenter presenter;

    DescriptorValidator(DescriptorValidatorOdmOutboundPort odmOutboundPort, DescriptorValidatorParserOutboundPort parserOutboundPort, DescriptorValidatorParametersOutboundPort parametersOutboundPort, DescriptorValidatorPresenter presenter) {
        this.odmOutboundPort = odmOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.parametersOutboundPort = parametersOutboundPort;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        try {
            JsonNode dataProductDescriptor = parserOutboundPort.getCanonicalRawDescriptor();
            List<String> policyEvents = parametersOutboundPort.getPolicyEvents();
            presenter.presentValidationResults(odmOutboundPort.validateDataProduct(dataProductDescriptor, policyEvents));
        } catch (Exception e) {
            presenter.presentException(e);
        }
    }

}

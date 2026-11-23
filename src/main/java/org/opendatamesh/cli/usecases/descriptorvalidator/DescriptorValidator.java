package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.List;

class DescriptorValidator implements UseCase {

    private final DescriptorValidatorCommand command;
    private final DescriptorValidatorPresenter presenter;
    private final DescriptorValidatorOdmOutboundPort odmOutboundPort;
    private final DescriptorValidatorParserOutboundPort parserOutboundPort;

    DescriptorValidator(DescriptorValidatorCommand command, DescriptorValidatorPresenter presenter, DescriptorValidatorOdmOutboundPort odmOutboundPort, DescriptorValidatorParserOutboundPort parserOutboundPort) {
        this.command = command;
        this.presenter = presenter;
        this.odmOutboundPort = odmOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
    }

    @Override
    public void execute() {
        try {
            checkDescriptorFile();
            JsonNode dataProductDescriptor = parserOutboundPort.getCanonicalRawDescriptor();
            List<String> policyEvents = getPolicyEvents();
            presenter.presentValidationResults(odmOutboundPort.validateDataProduct(dataProductDescriptor, policyEvents));
        } catch (Exception e) {
            presenter.presentException(e);
        }
    }

    private void checkDescriptorFile() {
        String descriptorFilePath = command.descriptorFilePath();
        if (descriptorFilePath == null || descriptorFilePath.trim().isEmpty()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Descriptor root path must not be empty.");
        }
        if (!parserOutboundPort.descriptorExists()) {
            throw new UseCaseException(UseCaseException.Reason.NOT_FOUND, "Descriptor root path does not exist: " + descriptorFilePath);
        }
        if (parserOutboundPort.isDescriptorDirectory()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Descriptor root path must be a file, not a directory: " + descriptorFilePath);
        }
        if (parserOutboundPort.isDescriptorEmpty()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Descriptor file is empty: " + descriptorFilePath);
        }
    }

    private List<String> getPolicyEvents() {
        //Supporting only one event at time
        if (command.policyEvent() != null) {
            return List.of(command.policyEvent());
        }
        return List.of();
    }

}

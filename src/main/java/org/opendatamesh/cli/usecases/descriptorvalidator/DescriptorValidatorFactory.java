package org.opendatamesh.cli.usecases.descriptorvalidator;

import org.opendatamesh.cli.clients.platform.registry.PlatformRegistryClientFactory;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DescriptorValidatorFactory {

    @Autowired
    private PlatformRegistryClientFactory registryClientFactory;

    public UseCase buildDescriptorValidator(DescriptorValidatorCommand command, DescriptorValidatorPresenter presenter) {
        DescriptorValidatorParserOutboundPort parserOutboundPort = new DescriptorValidatorParserOutboundPortImpl(command.descriptorFilePath());
        DescriptorValidatorOdmOutboundPort odmOutboundPort = new DescriptorValidatorOdmOutboundPortImpl(registryClientFactory.getClient());
        return new DescriptorValidator(command, presenter, odmOutboundPort, parserOutboundPort);
    }
}

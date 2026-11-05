package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductDescriptorInitializerFactory {

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    public UseCase buildDataProductDescriptorInitializer(DataProductDescriptorInitializerCommand command, DataProductDescriptorInitializerPresenter presenter) {
        DataProductDescriptorInitializerParserOutboundPort parserOutboundPort = new DataProductDescriptorInitializerParserOutboundPortImpl(odmCliConfiguration);
        return new DataProductDescriptorInitializer(command, presenter, parserOutboundPort);
    }
}

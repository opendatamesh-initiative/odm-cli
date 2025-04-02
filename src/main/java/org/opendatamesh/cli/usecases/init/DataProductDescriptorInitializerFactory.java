package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataProductDescriptorInitializerFactory {

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    public UseCase getImportSchemaUseCase(
            Map<String, String> importSchemaCommandParams
    ) {
        DataProductDescriptorInitializerParameterOutboundPort parameterOutboundPort = new DataProductDescriptorInitializerParameterOutboundPortImpl(
                odmCliConfiguration,
                importSchemaCommandParams
        );
        DataProductDescriptorInitializerParserOutboundPort parserOutboundPort = new DataProductDescriptorInitializerParserOutboundPortImpl(odmCliConfiguration);
        return new DataProductDescriptorInitializer(parameterOutboundPort, parserOutboundPort);
    }
}

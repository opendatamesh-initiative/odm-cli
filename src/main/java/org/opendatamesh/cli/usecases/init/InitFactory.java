package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class InitFactory {

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    public UseCase getImportSchemaUseCase(
            Map<String, String> importSchemaCommandParams
    ) {
        InitParameterOutboundPort parameterOutboundPort = new InitParameterOutboundPortImpl(
                odmCliConfiguration,
                importSchemaCommandParams
        );
        InitParserOutboundPort parserOutboundPort = new InitParserOutboundPortImpl(odmCliConfiguration);
        return new Init(parameterOutboundPort, parserOutboundPort);
    }
}

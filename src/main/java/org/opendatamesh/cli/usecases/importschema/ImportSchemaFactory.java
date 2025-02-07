package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ImportSchemaFactory {

    @Autowired
    private OdmCliBaseConfiguration odmCliBaseConfiguration;

    public UseCase getImportSchemaUseCase(
            String descriptorFilePath,
            Map<String, String> importSchemaCommandParams,
            ImportSchemaExtension importSchemaExtension
    ) {
        ImportSchemaParameterOutboundPort parameterOutboundPort = new ImportSchemaParameterOutboundPortImpl(
                odmCliBaseConfiguration,
                descriptorFilePath,
                importSchemaCommandParams
        );
        ImportSchemaParserOutboundPort parserOutboundPort = new ImportSchemaParserOutboundPortImpl();
        return new ImportSchema(parameterOutboundPort, parserOutboundPort, importSchemaExtension);
    }
}

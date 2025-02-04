package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.ExtensionsLoader;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ImportSchemaFactory {

    @Autowired
    private ExtensionsLoader extensionsLoader;

    public UseCase getImportSchemaUseCase(
            String descriptorFilePath,
            String from,
            String to,
            Map<String, String> inParams,
            Map<String, String> outParams
    ) {
        ImportSchemaParameterOutboundPort parameterOutboundPort = new ImportSchemaParameterOutboundPortImpl(
                descriptorFilePath,
                from,
                to,
                inParams,
                outParams
        );
        ImportSchemaExtension importSchemaExtension = extensionsLoader.getImportSchemaExtension(from, to);
        ImportSchemaParserOutboundPort parserOutboundPort = new ImportSchemaParserOutboundPortImpl();
        return new ImportSchema(parameterOutboundPort, parserOutboundPort, importSchemaExtension);
    }
}

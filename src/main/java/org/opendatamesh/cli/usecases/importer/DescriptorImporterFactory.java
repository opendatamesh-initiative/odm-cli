package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Map;

@Component
public class DescriptorImporterFactory {

    private final OdmCliConfiguration odmCliConfiguration;

    @Autowired
    public DescriptorImporterFactory(OdmCliConfiguration odmCliConfiguration) {
        this.odmCliConfiguration = odmCliConfiguration;
    }

    UseCase getImportSchemaUseCase(String descriptorFilePath, Map<String, String> importSchemaCommandParams, ImporterExtension importerExtension) {
        ImporterParameterOutboundPort parameterOutboundPort = new ImporterParameterOutboundPortImpl(
                odmCliConfiguration,
                descriptorFilePath,
                importSchemaCommandParams
        );
        ImporterParserOutboundPort parserOutboundPort = new PortImporterParserOutboundPortImpl(Paths.get(descriptorFilePath), odmCliConfiguration);
        return new DescriptorImporter(parameterOutboundPort, parserOutboundPort, importerExtension);
    }
}

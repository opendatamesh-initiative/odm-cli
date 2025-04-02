package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ImporterFactory {
    private final PortImporterFactory portImporterFactory;
    private final DescriptorImporterFactory descriptorImporterFactory;

    @Autowired
    public ImporterFactory(DescriptorImporterFactory descriptorImporterFactory, PortImporterFactory portImporterFactory) {
        this.descriptorImporterFactory = descriptorImporterFactory;
        this.portImporterFactory = portImporterFactory;
    }

    public UseCase getImportSchemaUseCase(
            String descriptorFilePath,
            Map<String, String> importSchemaCommandParams,
            ImporterExtension importSchemaExtension
    ) {
        String toOption = importSchemaCommandParams.get("to");
        if("descriptor".equalsIgnoreCase(toOption)){
            return descriptorImporterFactory.getImportSchemaUseCase(descriptorFilePath,importSchemaCommandParams,importSchemaExtension);
        } else {
            return portImporterFactory.getImportSchemaUseCase(descriptorFilePath,importSchemaCommandParams,importSchemaExtension);
        }
    }
}

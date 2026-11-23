package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImporterFactory {
    private final PortImporterFactory portImporterFactory;
    private final DescriptorImporterFactory descriptorImporterFactory;

    @Autowired
    public ImporterFactory(DescriptorImporterFactory descriptorImporterFactory, PortImporterFactory portImporterFactory) {
        this.descriptorImporterFactory = descriptorImporterFactory;
        this.portImporterFactory = portImporterFactory;
    }

    public UseCase buildImporter(ImporterCommand command, ImporterExtension importSchemaExtension, ImporterPresenter presenter) {
        if("descriptor".equalsIgnoreCase(command.to())){
            return descriptorImporterFactory.buildDescriptorImporter(command, importSchemaExtension, presenter);
        } else {
            return portImporterFactory.buildPortImporter(command, importSchemaExtension, presenter);
        }
    }
}

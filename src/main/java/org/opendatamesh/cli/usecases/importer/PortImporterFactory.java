package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortImporterFactory {

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    public UseCase buildPortImporter(ImporterCommand command, ImporterExtension importSchemaExtension, ImporterPresenter presenter) {
        ImporterConfigurationOutboundPort configurationOutboundPort = new ImporterConfigurationOutboundPortImpl(odmCliConfiguration);
        ImporterParserOutboundPort parserOutboundPort = new PortImporterParserOutboundPortImpl(command.descriptorFilePath(), odmCliConfiguration);
        return new PortImporter(command, presenter, configurationOutboundPort, parserOutboundPort, importSchemaExtension);
    }
}

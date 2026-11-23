package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DescriptorImporterFactory {

    private final OdmCliConfiguration odmCliConfiguration;

    @Autowired
    public DescriptorImporterFactory(OdmCliConfiguration odmCliConfiguration) {
        this.odmCliConfiguration = odmCliConfiguration;
    }

    UseCase buildDescriptorImporter(ImporterCommand command, ImporterExtension importerExtension, ImporterPresenter presenter) {
        ImporterConfigurationOutboundPort configurationOutboundPort = new ImporterConfigurationOutboundPortImpl(odmCliConfiguration);
        ImporterParserOutboundPort parserOutboundPort = new PortImporterParserOutboundPortImpl(command.descriptorFilePath(), odmCliConfiguration);
        return new DescriptorImporter(command, presenter, configurationOutboundPort, parserOutboundPort, importerExtension);
    }
}

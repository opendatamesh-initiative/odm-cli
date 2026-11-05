package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersion;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class DescriptorImporter implements UseCase {

    private final ImporterCommand command;
    private final ImporterPresenter presenter;
    private final ImporterConfigurationOutboundPort configurationOutboundPort;
    private final ImporterParserOutboundPort parserOutboundPort;
    private final ImporterExtension<DataProductVersion> importerExtension;

    DescriptorImporter(ImporterCommand command, ImporterPresenter presenter, ImporterConfigurationOutboundPort configurationOutboundPort, ImporterParserOutboundPort parserOutboundPort, ImporterExtension<DataProductVersion> importerExtension) {
        this.command = command;
        this.presenter = presenter;
        this.configurationOutboundPort = configurationOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importerExtension = importerExtension;
    }

    @Override
    public void execute() {
        try {
            DataProductVersion descriptor = readDescriptorOrCreateNew();
            ImporterArguments arguments = buildImporterArguments(descriptor);
            DataProductVersion newDescriptor = importerExtension.importElement(descriptor, arguments);
            parserOutboundPort.saveDescriptor(newDescriptor);
            presenter.presentDescriptorImported(newDescriptor);
        } catch (Exception e) {
            presenter.presentException(e);
        }
    }

    private ImporterArguments buildImporterArguments(DataProductVersion descriptor) {
        ImporterArguments arguments = new ImporterArguments();
        arguments.setRootDescriptorPath(Path.of(command.descriptorFilePath()));
        arguments.setOdmCliConfig(configurationOutboundPort.getOdmCliConfig());
        arguments.setParentCommandOptions(command.parentCommandOptions());
        arguments.setDataProductVersion(descriptor);
        return arguments;
    }

    private DataProductVersion readDescriptorOrCreateNew() {
        try {
            return parserOutboundPort.getDataProductVersion();
        } catch (FileNotFoundException e) {
            return new DataProductVersion();
        }
    }

}

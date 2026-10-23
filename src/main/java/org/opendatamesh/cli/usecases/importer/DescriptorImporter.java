package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersion;

import java.io.FileNotFoundException;

public class DescriptorImporter implements UseCase {

    private final ImporterParameterOutboundPort parameterOutboundPort;
    private final ImporterParserOutboundPort parserOutboundPort;
    private final ImporterExtension<DataProductVersion> importerExtension;
    private final ImporterPresenter presenter;

    DescriptorImporter(ImporterParameterOutboundPort parameterOutboundPort, ImporterParserOutboundPort parserOutboundPort, ImporterExtension<DataProductVersion> importerExtension, ImporterPresenter presenter) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importerExtension = importerExtension;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        try {
            DataProductVersion descriptor = readDescriptorOrCreateNew();
            ImporterArguments arguments = parameterOutboundPort.getImporterArguments();
            arguments.setDataProductVersion(descriptor);
            DataProductVersion newDescriptor = importerExtension.importElement(descriptor, arguments);
            parserOutboundPort.saveDescriptor(newDescriptor);
            presenter.presentDescriptorImported(newDescriptor);
        } catch (Exception e) {
            presenter.presentException(e);
        }
    }

    private DataProductVersion readDescriptorOrCreateNew() {
        try {
            return parserOutboundPort.getDataProductVersion();
        } catch (FileNotFoundException e) {
            return new DataProductVersion();
        }
    }

}

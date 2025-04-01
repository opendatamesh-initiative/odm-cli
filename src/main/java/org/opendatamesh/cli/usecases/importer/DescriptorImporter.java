package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersion;

import java.io.FileNotFoundException;

public class DescriptorImporter implements UseCase {

    private final ImporterParameterOutboundPort parameterOutboundPort;
    private final ImporterParserOutboundPort parserOutboundPort;
    private final ImporterExtension<DataProductVersion> importerExtension;

    DescriptorImporter(ImporterParameterOutboundPort parameterOutboundPort, ImporterParserOutboundPort parserOutboundPort, ImporterExtension<DataProductVersion> importerExtension) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importerExtension = importerExtension;
    }

    @Override
    public void execute() {
        DataProductVersion descriptor;
        try {
            descriptor = parserOutboundPort.getDataProductVersion();
        } catch (FileNotFoundException e) {
           descriptor = new DataProductVersion();
        }
        ImporterArguments arguments = parameterOutboundPort.getImporterArguments();
        arguments.setDataProductVersion(descriptor);
        DataProductVersion newDescriptor = importerExtension.importElement(descriptor, arguments);
        parserOutboundPort.saveDescriptor(newDescriptor);
    }

}

package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaOptions;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;

class ImportSchema implements UseCase {

    private final ImportSchemaParameterOutboundPort parameterOutboundPort;
    private final ImportSchemaParserOutboundPort parserOutboundPort;
    private final ImportSchemaExtension importSchemaExtension;

    ImportSchema(ImportSchemaParameterOutboundPort parameterOutboundPort, ImportSchemaParserOutboundPort parserOutboundPort, ImportSchemaExtension importSchemaExtension) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importSchemaExtension = importSchemaExtension;
    }

    @Override
    public void execute() {
        DataProductVersionDPDS descriptor = parserOutboundPort.getDataProductVersion(parameterOutboundPort.getDescriptorPath());

        ImportSchemaOptions options = new ImportSchemaOptions();
        options.setRootDescriptorPath(parameterOutboundPort.getDescriptorPath());
        options.setCommandCliInputParameters(parameterOutboundPort.getInParams());
        options.setCommandCliOutputParameters(parameterOutboundPort.getOutParams());

        PortDPDS port = importSchemaExtension.importElement(options);
        descriptor.getInterfaceComponents().getOutputPorts().add(port);

        parserOutboundPort.saveDescriptor(descriptor, parameterOutboundPort.getDescriptorPath());
    }
}

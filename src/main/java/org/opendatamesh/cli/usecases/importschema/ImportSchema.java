package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.plugin.ImportPlugin;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;

class ImportSchema implements UseCase {

    private final ImportSchemaParameterOutboundPort parameterOutboundPort;
    private final ImportSchemaParserOutboundPort parserOutboundPort;
    private final ImportPlugin importPlugin;

    ImportSchema(ImportSchemaParameterOutboundPort parameterOutboundPort, ImportSchemaParserOutboundPort parserOutboundPort, ImportPlugin importPlugin) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importPlugin = importPlugin;
    }

    @Override
    public void execute() {
        DataProductVersionDPDS descriptor = parserOutboundPort.getDataProductVersion(parameterOutboundPort.getDescriptorPath());

        PortDPDS port = importPlugin.importElement(parameterOutboundPort.getDescriptorPath(), parameterOutboundPort.getOutParams());
        descriptor.getInterfaceComponents().getOutputPorts().add(port);

        parserOutboundPort.saveDescriptor(descriptor, parameterOutboundPort.getDescriptorPath());
    }
}

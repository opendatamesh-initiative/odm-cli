package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.dpds.model.interfaces.PortDPDS;

class ImportSchemaExtensionMockState {

    private PortDPDS outputPort;

    public PortDPDS getOutputPort() {
        return outputPort;
    }

    public void setOutputPort(PortDPDS outputPort) {
        this.outputPort = outputPort;
    }
}

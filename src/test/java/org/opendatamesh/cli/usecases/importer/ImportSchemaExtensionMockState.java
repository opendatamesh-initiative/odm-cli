package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.interfaces.PortDPDS;

class ImporterExtensionMockState {

    private PortDPDS outputPort;

    public PortDPDS getOutputPort() {
        return outputPort;
    }

    public void setOutputPort(PortDPDS outputPort) {
        this.outputPort = outputPort;
    }
}

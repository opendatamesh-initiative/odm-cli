package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.interfaces.Port;

class ImporterExtensionMockState {

    private Port outputPort;

    public Port getOutputPort() {
        return outputPort;
    }

    public void setOutputPort(Port outputPort) {
        this.outputPort = outputPort;
    }
}

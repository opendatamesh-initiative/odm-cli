package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

public class PortImporterParserOutboundPortMock implements PortImporterParserOutboundPort {

    private final ImportSchemaParserMockState state;

    public PortImporterParserOutboundPortMock(ImportSchemaParserMockState state) {
        this.state = state;
    }

    @Override
    public DataProductVersionDPDS getDataProductVersion() {
        return state.getDataProductVersion();
    }

    @Override
    public void saveDescriptor(DataProductVersionDPDS descriptor) {
        state.setDataProductVersion(descriptor);
    }
}

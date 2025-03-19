package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersion;

public class PortImporterParserOutboundPortMock implements PortImporterParserOutboundPort {

    private final ImportSchemaParserMockState state;

    public PortImporterParserOutboundPortMock(ImportSchemaParserMockState state) {
        this.state = state;
    }

    @Override
    public DataProductVersion getDataProductVersion() {
        return state.getDataProductVersion();
    }

    @Override
    public void saveDescriptor(DataProductVersion descriptor) {
        state.setDataProductVersion(descriptor);
    }
}

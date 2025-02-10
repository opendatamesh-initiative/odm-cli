package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

public class ImportSchemaParserOutboundPortMock implements ImportSchemaParserOutboundPort {

    private final ImportSchemaParserMockState state;

    public ImportSchemaParserOutboundPortMock(ImportSchemaParserMockState state) {
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

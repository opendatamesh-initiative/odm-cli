package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersion;

public class ImporterParserOutboundPortMock implements ImporterParserOutboundPort {

    private final ImporterParserMockState state;

    public ImporterParserOutboundPortMock(ImporterParserMockState state) {
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

package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

import java.nio.file.Path;

public class ImportSchemaParserOutboundPortMock implements ImportSchemaParserOutboundPort {

    private final ImportSchemaParserMockState state;

    public ImportSchemaParserOutboundPortMock(ImportSchemaParserMockState state) {
        this.state = state;
    }

    @Override
    public DataProductVersionDPDS getDataProductVersion(Path descriptorPath) {
        return null;
    }

    @Override
    public void saveDescriptor(DataProductVersionDPDS descriptor, Path descriptorPath) {

    }
}

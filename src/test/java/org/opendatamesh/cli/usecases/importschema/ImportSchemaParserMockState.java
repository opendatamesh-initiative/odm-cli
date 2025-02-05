package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

public class ImportSchemaParserMockState {
    private DataProductVersionDPDS dataProductVersion;

    public DataProductVersionDPDS getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionDPDS dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

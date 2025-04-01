package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersion;

public class ImporterParserMockState {
    private DataProductVersion dataProductVersion;

    public DataProductVersion getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersion dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

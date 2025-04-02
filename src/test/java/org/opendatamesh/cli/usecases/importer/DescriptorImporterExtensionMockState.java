package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersion;

public class DescriptorImporterExtensionMockState {

    DataProductVersion dataProductVersion;

    public DataProductVersion getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersion dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

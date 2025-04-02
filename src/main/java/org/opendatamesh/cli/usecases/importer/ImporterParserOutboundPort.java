package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersion;

import java.io.FileNotFoundException;

interface ImporterParserOutboundPort {
    DataProductVersion getDataProductVersion() throws FileNotFoundException;

    void saveDescriptor(DataProductVersion descriptor);
}

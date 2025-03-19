package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersion;

interface PortImporterParserOutboundPort {
    DataProductVersion getDataProductVersion();

    void saveDescriptor(DataProductVersion descriptor);
}

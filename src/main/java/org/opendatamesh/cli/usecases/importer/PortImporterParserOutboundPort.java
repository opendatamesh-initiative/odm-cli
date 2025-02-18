package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

interface PortImporterParserOutboundPort {
    DataProductVersionDPDS getDataProductVersion();

    void saveDescriptor(DataProductVersionDPDS descriptor);
}

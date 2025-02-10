package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

interface ImportSchemaParserOutboundPort {
    DataProductVersionDPDS getDataProductVersion();

    void saveDescriptor(DataProductVersionDPDS descriptor);
}

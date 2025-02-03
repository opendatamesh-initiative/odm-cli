package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.dpds.model.DataProductVersionDPDS;

import java.nio.file.Path;

interface ImportSchemaParserOutboundPort {
    DataProductVersionDPDS getDataProductVersion(Path descriptorPath);

    void saveDescriptor(DataProductVersionDPDS descriptor, Path descriptorPath);
}

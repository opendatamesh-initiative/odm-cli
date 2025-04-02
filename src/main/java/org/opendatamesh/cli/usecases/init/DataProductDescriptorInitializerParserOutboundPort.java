package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.dpds.model.DataProductVersion;

interface DataProductDescriptorInitializerParserOutboundPort {
    DataProductVersion getDataProductVersion();

    <T> void saveObject(T object, String outputFiletype, boolean force);
}

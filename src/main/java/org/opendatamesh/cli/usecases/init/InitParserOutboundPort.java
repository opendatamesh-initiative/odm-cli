package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.dpds.model.DataProductVersion;

interface InitParserOutboundPort {
    DataProductVersion getDataProductVersion();

    <T> void saveObject(T object, String outputFiletype, boolean force);
}

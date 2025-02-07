package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;

import java.nio.file.Path;
import java.util.Map;

interface ImportSchemaParameterOutboundPort {
    Path getDescriptorPath();

    OdmCliBaseConfiguration getOdmClientConfig();

    Map<String, String> getImportSchemaCommandParams();
}

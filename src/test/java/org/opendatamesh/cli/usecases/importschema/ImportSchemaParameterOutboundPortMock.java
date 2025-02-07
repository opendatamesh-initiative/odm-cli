package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;

import java.nio.file.Path;
import java.util.Map;

class ImportSchemaParameterOutboundPortMock implements ImportSchemaParameterOutboundPort {

    @Override
    public Path getDescriptorPath() {
        return Path.of("");
    }

    @Override
    public OdmCliBaseConfiguration getOdmClientConfig() {
        return new OdmCliBaseConfiguration();
    }

    @Override
    public Map<String, String> getImportSchemaCommandParams() {
        return Map.of();
    }
}

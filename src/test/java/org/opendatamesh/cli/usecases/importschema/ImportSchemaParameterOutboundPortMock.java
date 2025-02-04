package org.opendatamesh.cli.usecases.importschema;

import java.nio.file.Path;
import java.util.Map;

class ImportSchemaParameterOutboundPortMock implements ImportSchemaParameterOutboundPort {

    private final ImportSchemaParameterMockState state;

    ImportSchemaParameterOutboundPortMock(ImportSchemaParameterMockState state) {
        this.state = state;
    }

    @Override
    public Path getDescriptorPath() {
        return null;
    }

    @Override
    public String getFrom() {
        return "";
    }

    @Override
    public String getTo() {
        return "";
    }

    @Override
    public Map<String, String> getInParams() {
        return Map.of();
    }

    @Override
    public Map<String, String> getOutParams() {
        return Map.of();
    }
}

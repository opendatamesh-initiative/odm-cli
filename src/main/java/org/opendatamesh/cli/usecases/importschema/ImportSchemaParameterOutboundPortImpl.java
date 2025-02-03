package org.opendatamesh.cli.usecases.importschema;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

class ImportSchemaParameterOutboundPortImpl implements ImportSchemaParameterOutboundPort {

    private final Path descriptorFilePath;
    private final String from;
    private final String to;
    private final Map<String, String> inParams;
    private final Map<String, String> outParams;

    ImportSchemaParameterOutboundPortImpl(String descriptorRootFilePath, String from, String to, Map<String, String> inParams, Map<String, String> outParams) {
        this.descriptorFilePath = Path.of(descriptorRootFilePath);
        validateDescriptorFilePath(descriptorRootFilePath);
        this.from = from;
        this.to = to;
        this.inParams = inParams;
        this.outParams = outParams;
        //TODO validate and setDefaultParmsForOutputPortTarget(outParamMap);
    }

    private void validateDescriptorFilePath(String descriptorRootFilePath) {
        if (descriptorRootFilePath.isEmpty()) {
            throw new IllegalArgumentException("Invalid descriptor root path.");
        }
        try {
            Path path = Paths.get(descriptorRootFilePath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Descriptor root path does not exist: " + descriptorRootFilePath);
            }
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("Descriptor root path is not a directory: " + descriptorRootFilePath);
            }
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException("Invalid descriptor root path: " + ex.getMessage(), ex);
        }

    }

    @Override
    public Path getDescriptorPath() {
        return this.descriptorFilePath;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public Map<String, String> getInParams() {
        return inParams;
    }

    @Override
    public Map<String, String> getOutParams() {
        return outParams;
    }
}

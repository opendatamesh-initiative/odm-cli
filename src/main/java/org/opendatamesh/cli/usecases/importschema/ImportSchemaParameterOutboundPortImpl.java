package org.opendatamesh.cli.usecases.importschema;

import java.io.IOException;
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
        if (descriptorRootFilePath == null || descriptorRootFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Descriptor root path must not be empty.");
        }

        try {
            Path path = Paths.get(descriptorRootFilePath);

            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Descriptor root path does not exist: " + descriptorRootFilePath);
            }

            if (Files.isDirectory(path)) {
                throw new IllegalArgumentException("Descriptor root path must be a file, not a directory: " + descriptorRootFilePath);
            }

            if (Files.size(path) == 0) {
                throw new IllegalArgumentException("Descriptor file is empty: " + descriptorRootFilePath);
            }

        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException("Invalid descriptor root path: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read the descriptor file: " + ex.getMessage(), ex);
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

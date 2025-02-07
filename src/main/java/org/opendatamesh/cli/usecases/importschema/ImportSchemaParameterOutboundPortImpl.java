package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

class ImportSchemaParameterOutboundPortImpl implements ImportSchemaParameterOutboundPort {

    private final OdmCliBaseConfiguration odmCliBaseConfiguration;
    private final Path descriptorFilePath;
    private final Map<String, String> importSchemaCommandParams;

    ImportSchemaParameterOutboundPortImpl(
            OdmCliBaseConfiguration odmCliBaseConfiguration,
            String descriptorRootFilePath,
            Map<String, String> importSchemaCommandParams
    ) {
        this.odmCliBaseConfiguration = odmCliBaseConfiguration;
        this.descriptorFilePath = Path.of(descriptorRootFilePath);
        validateDescriptorFilePath(descriptorRootFilePath);
        this.importSchemaCommandParams = importSchemaCommandParams;
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
    public OdmCliBaseConfiguration getOdmClientConfig() {
        return this.odmCliBaseConfiguration;
    }

    @Override
    public Map<String, String> getImportSchemaCommandParams() {
        return importSchemaCommandParams;
    }

}

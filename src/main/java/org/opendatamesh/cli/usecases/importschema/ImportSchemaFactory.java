package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class ImportSchemaFactory {

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    public UseCase getImportSchemaUseCase(
            String descriptorFilePath,
            Map<String, String> importSchemaCommandParams,
            ImportSchemaExtension importSchemaExtension
    ) {
        validateDescriptorFilePath(descriptorFilePath);
        ImportSchemaParameterOutboundPort parameterOutboundPort = new ImportSchemaParameterOutboundPortImpl(
                odmCliConfiguration,
                descriptorFilePath,
                importSchemaCommandParams
        );
        ImportSchemaParserOutboundPort parserOutboundPort = new ImportSchemaParserOutboundPortImpl(Paths.get(descriptorFilePath), odmCliConfiguration);
        return new ImportSchema(parameterOutboundPort, parserOutboundPort, importSchemaExtension);
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
}

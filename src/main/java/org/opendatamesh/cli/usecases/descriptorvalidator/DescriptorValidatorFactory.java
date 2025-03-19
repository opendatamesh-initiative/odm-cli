package org.opendatamesh.cli.usecases.descriptorvalidator;

import org.opendatamesh.cli.clients.platform.registry.OdmPlatformRegistryClientFactory;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.UseCaseReturning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class DescriptorValidatorFactory {

    @Autowired
    private OdmCliConfiguration cliConfiguration;

    @Autowired
    private OdmPlatformRegistryClientFactory registryClientFactory;

    public UseCaseReturning<DataProductValidationResults> getDescriptorValidatorUseCase(
            String descriptorFilePath,
            Map<String, String> validateCommandParams
    ) {
        validateDescriptorFilePath(descriptorFilePath);
        DescriptorValidatorParserOutboundPort parserOutboundPort = new DescriptorValidatorParserOutboundPortImpl(Paths.get(descriptorFilePath));
        DescriptorValidatorOdmOutboundPort odmOutboundPort = new DescriptorValidatorOdmOutboundPortImpl(registryClientFactory.getClient());
        DescriptorValidatorParametersOutboundPort parametersOutboundPort = new DescriptorValidatorParametersOutboundPortImpl(
                cliConfiguration,
                descriptorFilePath,
                validateCommandParams
        );
        return new DescriptorValidator(odmOutboundPort, parserOutboundPort, parametersOutboundPort);
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

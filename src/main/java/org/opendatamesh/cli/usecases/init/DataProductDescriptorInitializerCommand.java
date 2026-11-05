package org.opendatamesh.cli.usecases.init;

// Optional fields are null when not provided: the use case applies the default values
public record DataProductDescriptorInitializerCommand(
        String domain,
        String name,
        String displayName,
        String descriptorVersion,
        String fullyQualifiedName,
        String description,
        String ownerId,
        String ownerName,
        String outputFile,
        boolean force
) {
}

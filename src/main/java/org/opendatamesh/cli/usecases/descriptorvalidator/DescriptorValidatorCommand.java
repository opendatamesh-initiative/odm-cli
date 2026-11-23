package org.opendatamesh.cli.usecases.descriptorvalidator;

// policyEvent is optional: when null the descriptor is validated against all the policies
public record DescriptorValidatorCommand(String descriptorFilePath, String policyEvent) {
}

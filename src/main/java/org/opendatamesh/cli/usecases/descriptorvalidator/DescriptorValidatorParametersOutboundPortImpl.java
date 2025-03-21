package org.opendatamesh.cli.usecases.descriptorvalidator;

import org.opendatamesh.cli.configs.OdmCliConfiguration;

import java.util.List;
import java.util.Map;

class DescriptorValidatorParametersOutboundPortImpl implements DescriptorValidatorParametersOutboundPort {

    private final OdmCliConfiguration cliConfiguration;
    private final String descriptorFilePath;
    private final Map<String, String> validateCommandParams;

    DescriptorValidatorParametersOutboundPortImpl(OdmCliConfiguration cliConfiguration, String descriptorFilePath, Map<String, String> validateCommandParams) {
        this.cliConfiguration = cliConfiguration;
        this.descriptorFilePath = descriptorFilePath;
        this.validateCommandParams = validateCommandParams;
    }

    @Override
    public List<String> getPolicyEvents() {
        //Supporting only one event at time
        if (validateCommandParams.get("event") != null) {
            return List.of(validateCommandParams.get("event"));
        }
        return List.of();
    }
}

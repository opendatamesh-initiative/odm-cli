package org.opendatamesh.cli.commands.platform.registry.validate;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.descriptorvalidator.DataProductValidationResults;
import org.opendatamesh.cli.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidateCommandExecutor extends PicoCliCommandExecutor {

    private final DescriptorValidatorFactory validatorFactory;

    private final Map<String, String> validateCommandParams = new HashMap<>();
    private String descriptorFilePath;
    private String isVerbose;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ValidateCommandExecutor(OdmCliConfiguration odmCliConfiguration, DescriptorValidatorFactory validatorFactory) {
        super(odmCliConfiguration);
        this.validatorFactory = validatorFactory;
    }

    @Override
    protected void handleRequiredOptions(Boolean interactive) {
        super.handleRequiredOptions(interactive);

        if (!StringUtils.hasText(descriptorFilePath)) {
            if (Boolean.TRUE.equals(interactive)) {
                descriptorFilePath = System.console().readLine(
                        String.format("Enter value for %s: ",
                                "descriptor file path"
                        )
                );
            } else {
                throw new IllegalStateException("Missing value for required parameter: " + "descriptor file path.");
            }

        }
    }

    @Override
    protected void executeUseCase() {
        super.executeUseCase();
        DataProductValidationResults validationResults = validatorFactory
                .getDescriptorValidatorUseCase(descriptorFilePath, validateCommandParams)
                .execute();
        logValidationResults(validationResults);
    }

    private void logValidationResults(DataProductValidationResults results) {
        if (results == null || CollectionUtils.isEmpty(results.getResults())) {
            logger.info("No validation results to log.");
            return;
        }

        for (DataProductValidationResults.Result result : results.getResults()) {
            if ("true".equalsIgnoreCase(this.isVerbose)) {
                String validationOutputStr = result.getValidationOutput() != null ? result.getValidationOutput().toString() : "none";
                logger.info("[Validation Result] Name: {}, Validated: {}, Blocking: {}, Output: {}", result.getName(), result.isValidated(), result.getBlockingFlag(), validationOutputStr);
            } else {
                logger.info("[Validation Result] Name: {}, Validated: {}", result.getName(), result.isValidated());
            }
        }
    }

    public void setValidateCommandParams(String commandName, String commandValue) {
        if (commandValue == null) {
            return;
        }
        this.validateCommandParams.put(commandName, commandValue);
    }

    public void setDescriptorFilePath(String descriptorFilePath) {
        this.descriptorFilePath = descriptorFilePath;
    }

    public void setIsVerbose(String isVerbose) {
        this.isVerbose = isVerbose;
    }
}

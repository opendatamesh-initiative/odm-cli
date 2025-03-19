package org.opendatamesh.cli.commands.platform.registry.validate;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.commands.platform.registry.RegistryCommandBuilder.REGISTRY_COMMAND;

@Component
public class ValidateCommandBuilder implements PicoCliCommandBuilder {
    public static final String REGISTRY_VALIDATE_COMMAND = "validate";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Autowired
    private DescriptorValidatorFactory validatorFactory;

    @Override
    public CommandLine buildCommand(String... args) {
        ValidateCommandExecutor executor = new ValidateCommandExecutor(odmCliConfiguration, validatorFactory);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(REGISTRY_VALIDATE_COMMAND);
        spec.version("1.0.0");
        spec.usageMessage().description("Command to validate a data product descriptor.");
        spec.mixinStandardHelpOptions(true);

        handleWithOrder(Lists.newArrayList(
                order -> handleFileParam(spec, order, executor),
                order -> handlePolicyEventParam(spec, order, executor),
                order -> handleVerboseParam(spec, order, executor)
        ));

        commands.stream().filter(command -> REGISTRY_VALIDATE_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    private void handleFileParam(CommandLine.Model.CommandSpec spec, int order, ValidateCommandExecutor executor) {
        CommandLine.Model.OptionSpec descriptorFilePathOption = CommandLine.Model.OptionSpec
                .builder("-f", "--file")
                .order(order)
                .description("Name of the descriptor file (default: PATH/data-product-descriptor.json)")
                .paramLabel("FILE")
                .required(false)
                .defaultValue("./data-product-descriptor.json")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setDescriptorFilePath((String) value);
                        executor.setValidateCommandParams("file", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorFilePathOption);
    }

    private void handlePolicyEventParam(CommandLine.Model.CommandSpec spec, int order, ValidateCommandExecutor executor) {
        CommandLine.Model.OptionSpec policyEventOption = CommandLine.Model.OptionSpec
                .builder("-e", "--event")
                .order(order)
                .description("If set, the descriptor is validated only against policies that are triggered by the given event. (e.g. `DATA_PRODUCT_CREATION`, `DATA_PRODUCT_UPDATE`, `DATA_PRODUCT_VERSION_CREATION`)")
                .paramLabel("POLICY_EVALUATION_EVENT")
                .required(false)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setValidateCommandParams("event", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(policyEventOption);
    }

    private void handleVerboseParam(CommandLine.Model.CommandSpec spec, int order, ValidateCommandExecutor executor) {
        CommandLine.Model.OptionSpec policyEventOption = CommandLine.Model.OptionSpec
                .builder("-ve", "--verbose")
                .order(order)
                .description("If `true`, the output of each validation result will be printed")
                .paramLabel("BOOLEAN")
                .required(false)
                .defaultValue("false")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setIsVerbose((String) value);
                        executor.setValidateCommandParams("verbose", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(policyEventOption);
    }

    @Override
    public String getParentCommandName() {
        return REGISTRY_COMMAND;
    }

    @Override
    public String getCommandName() {
        return REGISTRY_VALIDATE_COMMAND;
    }

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }
}

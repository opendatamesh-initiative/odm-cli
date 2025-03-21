package org.opendatamesh.cli.commands;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.function.IntConsumer;

@Component
public class OdmCliRootCommandBuilder implements PicoCliCommandBuilder {

    public static final String ODM_CLI_COMMAND = "odm-cli";
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Override
    public CommandLine buildCommand(String... args) {
        try {
            OdmCliRootCommandExecutor executor = new OdmCliRootCommandExecutor(odmCliConfiguration);
            CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
            spec.name(ODM_CLI_COMMAND);
            spec.usageMessage().description("ODM CLI init method");
            spec.version("1.0.0");
            spec.mixinStandardHelpOptions(true);

            handleWithOrder(Lists.newArrayList(
                    order -> handleInteractiveOption(spec, order)
            ));

            commands.stream().filter(command -> ODM_CLI_COMMAND.equals(command.getParentCommandName()))
                    .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
            return new CommandLine(spec);
        } catch (Exception e) {
            log.error("Failed to build command, cause: {}", e.getMessage(), e);
            return new CommandLine(CommandLine.Model.CommandSpec.create());
        }
    }

    @Override
    public String getParentCommandName() {
        return null;
    }

    @Override
    public String getCommandName() {
        return ODM_CLI_COMMAND;
    }

    private void handleInteractiveOption(CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorFilePathOption = CommandLine.Model.OptionSpec
                .builder("-i", "--interactive")
                .order(order)
                .description("Enables or disables interactive mode.")
                .paramLabel("BOOLEAN")
                .required(true)
                .defaultValue(odmCliConfiguration.getCliConfiguration().isInteractive() == null ? "true" : String.valueOf(odmCliConfiguration.getCliConfiguration().isInteractive()))
                .type(Boolean.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        odmCliConfiguration.getCliConfiguration().setInteractive((Boolean) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorFilePathOption);
    }

    //============= Utility code ===============================================================

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }
}

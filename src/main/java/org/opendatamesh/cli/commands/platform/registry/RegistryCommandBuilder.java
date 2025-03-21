package org.opendatamesh.cli.commands.platform.registry;

import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

import static org.opendatamesh.cli.commands.platform.PlatformCommandBuilder.PLATFORM_COMMAND;

@Component
public class RegistryCommandBuilder implements PicoCliCommandBuilder {
    public static final String REGISTRY_COMMAND = "registry";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Override
    public CommandLine buildCommand(String... args) {
        RegistryCommandExecutor executor = new RegistryCommandExecutor(odmCliConfiguration);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(REGISTRY_COMMAND);
        spec.version("1.0.0");
        spec.usageMessage().description("Command to manage the ODM Platform functionalities exposed by the Registry Service.");
        spec.mixinStandardHelpOptions(true);

        commands.stream().filter(command -> REGISTRY_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    @Override
    public String getParentCommandName() {
        return PLATFORM_COMMAND;
    }

    @Override
    public String getCommandName() {
        return REGISTRY_COMMAND;
    }

}

package org.opendatamesh.cli.commands.platform;

import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

import static org.opendatamesh.cli.commands.OdmCliRootCommandBuilder.ODM_CLI_COMMAND;

@Component
public class PlatformCommandBuilder implements PicoCliCommandBuilder {
    public static final String PLATFORM_COMMAND = "platform";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Override
    public CommandLine buildCommand(String... args) {
        PlatformCommandExecutor executor = new PlatformCommandExecutor(odmCliConfiguration);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(PLATFORM_COMMAND);
        spec.version("1.0.0");
        spec.usageMessage().description("Command to manage ODM Platform functionalities.");
        spec.mixinStandardHelpOptions(true);

        commands.stream().filter(command -> PLATFORM_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }


    @Override
    public String getParentCommandName() {
        return ODM_CLI_COMMAND;
    }

    @Override
    public String getCommandName() {
        return PLATFORM_COMMAND;
    }

}

package org.opendatamesh.cli.commands.platform.devops;

import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

import static org.opendatamesh.cli.commands.platform.PlatformCommandBuilder.PLATFORM_COMMAND;

@Component
public class DevOpsCommandBuilder implements PicoCliCommandBuilder {
    public static final String DEVOPS_COMMAND = "devops";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Override
    public CommandLine buildCommand(String... args) {
        DevOpsCommandExecutor executor = new DevOpsCommandExecutor(odmCliConfiguration);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(DEVOPS_COMMAND);
        spec.usageMessage().description("Command to run tasks of the ODM Platform DevOps Service from a pipeline.");
        spec.mixinStandardHelpOptions(true);

        commands.stream().filter(command -> DEVOPS_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    @Override
    public String getParentCommandName() {
        return PLATFORM_COMMAND;
    }

    @Override
    public String getCommandName() {
        return DEVOPS_COMMAND;
    }
}

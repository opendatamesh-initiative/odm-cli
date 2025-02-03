package org.opendatamesh.cli.commands.local;

import org.opendatamesh.cli.commands.OdmCliRootCommandBuilder;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

@Component
public class LocalCommandBuilder implements PicoCliCommandBuilder {
    private static final String LOCAL_COMMAND = "local";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Override
    public CommandLine buildCommand() {
        LocalCommandExecutor executor = new LocalCommandExecutor();
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(LOCAL_COMMAND);
        spec.version("odm-cli local 1.0.0");
        spec.usageMessage().description("Manage local env");
        spec.mixinStandardHelpOptions(true);

        commands.stream().filter(command -> LOCAL_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand()));
        return new CommandLine(spec);
    }

    @Override
    public String getParentCommandName() {
        return new OdmCliRootCommandBuilder().getCommandName();
    }

    @Override
    public String getCommandName() {
        return LOCAL_COMMAND;
    }

}

package org.opendatamesh.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

@Component
public class OdmCliRootCommandBuilder implements PicoCliCommandBuilder {

    private static final String ODM_CLI_COMMAND = "odm-cli";
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Override
    public CommandLine buildCommand(String... args) {
        try {
            OdmCliRootCommandExecutor executor = new OdmCliRootCommandExecutor();
            CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
            spec.name(ODM_CLI_COMMAND);
            spec.usageMessage().description("ODM CLI init method");
            spec.version("odm-cli 1.0.0");
            spec.mixinStandardHelpOptions(true);

            commands.stream().filter(command -> ODM_CLI_COMMAND.equals(command.getParentCommandName()))
                    .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
            return new CommandLine(spec);
        } catch (Exception e) {
            log.error("Failed to build command, cause: {}", e.getMessage());
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
}

package org.opendatamesh.cli.commands.local;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.OdmCliRootCommandBuilder;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.importer.referencehandler.DescriptorFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.function.IntConsumer;

@Component
public class LocalCommandBuilder implements PicoCliCommandBuilder {
    private static final String LOCAL_COMMAND = "local";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Override
    public CommandLine buildCommand(String... args) {
        LocalCommandExecutor executor = new LocalCommandExecutor();
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(LOCAL_COMMAND);
        spec.version("odm-cli local 1.0.0");
        spec.usageMessage().description("Manage local env");
        spec.mixinStandardHelpOptions(true);

        handleWithOrder(Lists.newArrayList(
                order -> handleSaveFormatParam(spec, order)
        ));

        commands.stream().filter(command -> LOCAL_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    private void handleSaveFormatParam(CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorFilePathOption = CommandLine.Model.OptionSpec
                .builder("-s", "--save-format")
                .order(order)
                .description("Specifies the format in which the descriptor could be saved. If set to canonical, each descriptor component is stored as a separate file, and the ref field is populated. If set to normalized, the descriptor is saved as the final POJO is.")
                .paramLabel("canonical/normalized")
                .required(false)
                .defaultValue(odmCliConfiguration.getCliConfiguration().getSaveFormat() == null ? DescriptorFormat.NORMALIZED.name() : odmCliConfiguration.getCliConfiguration().getSaveFormat())
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        odmCliConfiguration.getCliConfiguration().setSaveFormat((String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorFilePathOption);
    }

    @Override
    public String getParentCommandName() {
        return new OdmCliRootCommandBuilder().getCommandName();
    }

    @Override
    public String getCommandName() {
        return LOCAL_COMMAND;
    }

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }
}

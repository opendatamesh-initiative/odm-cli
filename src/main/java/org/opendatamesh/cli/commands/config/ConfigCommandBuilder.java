package org.opendatamesh.cli.commands.config;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.commands.OdmCliRootCommandBuilder.ODM_CLI_COMMAND;

@Component
public class ConfigCommandBuilder implements PicoCliCommandBuilder {
    private static final String CONFIG_COMMAND = "config";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Override
    public CommandLine buildCommand(String... args) {
        ConfigCommandExecutor executor = new ConfigCommandExecutor(odmCliConfiguration);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(CONFIG_COMMAND);
        spec.version("odm-cli config 1.0.0");
        spec.usageMessage().description("Manages the configuration file.");
        spec.mixinStandardHelpOptions(true);

        handleWithOrder(Lists.newArrayList(
                order -> handlePrintOption(spec, order, executor),
                order -> handleSetOption(spec, order, executor),
                order -> handleUnsetOption(spec, order, executor),
                order -> handleSetArrayOption(spec, order, executor),
                order -> handleUnsetArrayOption(spec, order, executor)
        ));

        commands.stream().filter(command -> CONFIG_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    private void handlePrintOption(CommandLine.Model.CommandSpec spec, int order, ConfigCommandExecutor executor) {
        CommandLine.Model.OptionSpec unsetOption = CommandLine.Model.OptionSpec
                .builder("-p", "--print")
                .order(order)
                .description("Prints the current configuration.")
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setPrint(true);
                        return value;
                    }
                })
                .build();
        spec.addOption(unsetOption);
    }

    private void handleUnsetOption(CommandLine.Model.CommandSpec spec, int order, ConfigCommandExecutor executor) {
        CommandLine.Model.OptionSpec unsetOption = CommandLine.Model.OptionSpec
                .builder("-u", "--unset")
                .order(order)
                .description("Deletes a property of the configuration file. E.g. --unset cli.env.property")
                .arity("0..1")
                .parameterConsumer(new ConfigParamConsumer())
                .type(List.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        List<String> list = (value != null) ? (List<String>) value : new ArrayList<>();
                        executor.setAttributesToDelete(list);
                        return (T) list;
                    }
                })
                .build();
        spec.addOption(unsetOption);
    }

    private void handleSetOption(CommandLine.Model.CommandSpec spec, int order, ConfigCommandExecutor executor) {
        CommandLine.Model.OptionSpec unsetOption = CommandLine.Model.OptionSpec
                .builder("-s", "--set")
                .order(order)
                .description("Adds a property in the configuration file. E.g. --set cli.env.property=propretyValue")
                .arity("0..1")
                .parameterConsumer(new ConfigParamConsumer())
                .type(List.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        List<String> list = (value != null) ? (List<String>) value : new ArrayList<>();
                        executor.setSimpleAttributesToAdd(list);
                        return (T) list;
                    }
                })
                .build();
        spec.addOption(unsetOption);
    }

    private void handleUnsetArrayOption(CommandLine.Model.CommandSpec spec, int order, ConfigCommandExecutor executor) {
        CommandLine.Model.OptionSpec unsetOption = CommandLine.Model.OptionSpec
                .builder("-ua", "--unsetarray")
                .order(order)
                .description("Removes elements of array fields in the configuration file that matches the given properties. E.g. --unsetarray cli.env.arrayproperty  field=value")
                .arity("0..1")
                .parameterConsumer(new ConfigParamConsumer())
                .type(List.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        List<String> list = (value != null) ? (List<String>) value : new ArrayList<>();
                        executor.setArraysEntriesToDelete(list);
                        return (T) list;
                    }
                })
                .build();
        spec.addOption(unsetOption);
    }

    private void handleSetArrayOption(CommandLine.Model.CommandSpec spec, int order, ConfigCommandExecutor executor) {
        CommandLine.Model.OptionSpec unsetOption = CommandLine.Model.OptionSpec
                .builder("-sa", "--setarray")
                .order(order)
                .description("If not present, adds the specified array property  in the configuration file. Then, if specified, adds an element to the array with the given properties and values. E.g. --setarray cli.env.arrayproperty  field=value")
                .arity("0..1")
                .parameterConsumer(new ConfigParamConsumer())
                .type(List.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        List<String> list = (value != null) ? (List<String>) value : new ArrayList<>();
                        executor.setArraysEntriesToAdd(list);
                        return (T) list;
                    }
                })
                .build();
        spec.addOption(unsetOption);
    }


    @Override
    public String getParentCommandName() {
        return ODM_CLI_COMMAND;
    }

    @Override
    public String getCommandName() {
        return CONFIG_COMMAND;
    }

    //============= Utility code ===============================================================

    private static class ConfigParamConsumer implements CommandLine.IParameterConsumer {
        @Override
        public void consumeParameters(Stack<String> args,
                                      CommandLine.Model.ArgSpec argSpec,
                                      CommandLine.Model.CommandSpec commandSpec) {
            List<String> values = new ArrayList<>();
            while (!args.isEmpty() && !args.peek().startsWith("--")) {
                values.add(args.pop());
            }
            argSpec.setValue(values);
        }
    }

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }
}

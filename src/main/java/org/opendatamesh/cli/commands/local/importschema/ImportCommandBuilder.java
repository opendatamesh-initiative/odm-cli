package org.opendatamesh.cli.commands.local.importschema;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.commands.local.LocalCommandBuilder;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.ExtensionsLoader;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.usecases.importschema.ImportSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.utils.CommandOptionsUtils.getOptionFromArguments;

@Component
public class ImportCommandBuilder implements PicoCliCommandBuilder {

    @Autowired
    private ImportSchemaFactory importSchemaFactory;
    @Autowired
    private ExtensionsLoader extensionsLoader;

    private static final String IMPORT_COMMAND = "import";

    @Override
    public CommandLine buildCommand(String... args) {
        Optional<String> from = getOptionFromArguments(args, "--from");
        Optional<String> to = getOptionFromArguments(args, "--to");

        ImportSchemaExtension extension = from.isPresent() && to.isPresent() ? extensionsLoader.getImportSchemaExtension(from.get(), to.get()) : null;
        ImportCommandExecutor executor = new ImportCommandExecutor(importSchemaFactory, extension);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(IMPORT_COMMAND);
        spec.mixinStandardHelpOptions(true);
        spec.version("odm-cli local import 1.0.0");
        spec.usageMessage().description("Import schema into a descriptor file", extension == null ? "" : extension.getExtensionInfo().getDescription());
        spec.usageMessage().sortOptions(false);

        handleWithOrder(Lists.newArrayList(
                order -> handleDescriptorFilePathParam(executor, spec, order),
                order -> handleFromParam(executor, spec, order),
                order -> handleToParam(executor, spec, order),
                order -> {
                    if (extension != null) handleExtensionParameters(extension, spec, order);
                }
        ));
        return new CommandLine(spec);
    }

    @Override
    public String getParentCommandName() {
        return new LocalCommandBuilder().getCommandName();
    }

    @Override
    public String getCommandName() {
        return IMPORT_COMMAND;
    }

    private void handleDescriptorFilePathParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
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
                        executor.setImportSchemaCommandParam("file", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorFilePathOption);
    }

    private void handleFromParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec fromOption = CommandLine.Model.OptionSpec.builder("--from")
                .order(order)
                .description("Import source type (ex. ddl, jdbc, unity, etc...)")
                .required(true)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("from", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(fromOption);
    }

    private void handleToParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec toOption = CommandLine.Model.OptionSpec.builder("--to")
                .order(order)
                .description("Import target type (ex. output-port, input-port, api, schema, etc...)")
                .required(true)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("to", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(toOption);
    }

    private void handleExtensionParameters(ImportSchemaExtension extension, CommandLine.Model.CommandSpec commandSpec, int order) {
        for (ExtensionOption extensionOption : extension.getExtensionOptions()) {
            CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec
                    .builder(extensionOption.getNames().toArray(new String[0]))
                    .order(order)
                    .type(String.class)
                    .interactive(extensionOption.isInteractive())
                    .setter(new CommandLine.Model.ISetter() {
                        @Override
                        public <T> T set(T value) {
                            if (value != null) {
                                extensionOption.getSetter().accept((String) value);
                            }
                            return value;
                        }
                    });

            //Optional parameters
            if (extensionOption.getParamLabel() != null) {
                builder.paramLabel(extensionOption.getParamLabel());
            }
            if (extensionOption.getDescription() != null) {
                builder.description(extensionOption.getDescription());
            }
            if (extensionOption.getDefaultValue() != null) {
                builder.defaultValue(extensionOption.getDefaultValue());
            }

            CommandLine.Model.OptionSpec option = builder.build();
            order++;
            commandSpec.addOption(option);
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

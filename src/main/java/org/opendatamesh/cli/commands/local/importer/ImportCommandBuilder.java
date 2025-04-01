package org.opendatamesh.cli.commands.local.importer;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.ExtensionsLoader;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.importer.ImporterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.commands.local.LocalCommandBuilder.LOCAL_COMMAND;

@Component
public class ImportCommandBuilder implements PicoCliCommandBuilder {

    @Autowired
    private ImporterFactory importerFactory;
    @Autowired
    private ExtensionsLoader extensionsLoader;
    @Autowired
    private OdmCliConfiguration configuration;

    private static final String IMPORT_COMMAND = "import";

    @Override
    public CommandLine buildCommand(String... args) {
        Optional<String> from = getOptionFromArguments(args, "--from");
        Optional<String> to = getOptionFromArguments(args, "--to");

        ImporterExtension<?> extension = from.isPresent() && to.isPresent() ? extensionsLoader.getImporterExtension(from.get(), to.get()) : null;
        ImportCommandExecutor executor = new ImportCommandExecutor(configuration, importerFactory, extension);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(IMPORT_COMMAND);
        spec.mixinStandardHelpOptions(true);
        spec.version("1.0.0");
        spec.usageMessage().description("Import an object into a descriptor file", extension == null ? "" : extension.getExtensionInfo().getDescription());
        spec.usageMessage().sortOptions(false);

        handleWithOrder(Lists.newArrayList(
                order -> handleDescriptorFilePathParam(executor, spec, order),
                order -> handleFromParam(executor, spec, order),
                order -> handleToParam(executor, spec, order),
                order -> handleTargetParam(executor, spec, order),
                order -> handleSourceParam(executor, spec, order),
                order -> {
                    if (extension != null) handleExtensionParameters(extension, spec, order);
                }
        ));
        return new CommandLine(spec);
    }

    @Override
    public String getParentCommandName() {
        return LOCAL_COMMAND;
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
                .description("Import source type according to extension (ex. ddl, jdbc, unity, etc...)")
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
                .description("Import target object (ex. output-port, input-port, etc...)")
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

    private void handleTargetParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec targetOption = CommandLine.Model.OptionSpec.builder("--target")
                .order(order)
                .description("Import target name (ex. if --to=output-port, it is the name of the output-port to import)")
                .required(true)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("target", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(targetOption);
    }

    private void handleSourceParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec targetOption = CommandLine.Model.OptionSpec.builder("--source")
                .order(order)
                .description("Import source according to the import extension")
                .required(true)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("source", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(targetOption);
    }

    private void handleExtensionParameters(ImporterExtension extension, CommandLine.Model.CommandSpec commandSpec, int order) {
        for (ExtensionOption extensionOption : extension.getExtensionOptions()) {
            CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec
                    .builder(extensionOption.getNames().toArray(new String[0]))
                    .order(order)
                    .type(String.class)
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
            if (extensionOption.getDefaultValueFromConfig() != null) {
                Map<String, String> allConfig = configuration.getAllConfiguration();
                builder.defaultValue(allConfig.get(extensionOption.getDefaultValueFromConfig()));
            }

            CommandLine.Model.OptionSpec option = builder.build();
            order++;
            commandSpec.addOption(option);
        }
    }
    //============= Utility code ===============================================================

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }

    private Optional<String> getOptionFromArguments(String[] args, String option) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith(option)) {
                if (arg.contains("=")) {
                    return Optional.of(arg.substring(arg.indexOf('=') + 1));
                }
                if (i + 1 < args.length) {
                    return Optional.ofNullable(args[i + 1]);
                }
            }
        }
        return Optional.empty();
    }
}

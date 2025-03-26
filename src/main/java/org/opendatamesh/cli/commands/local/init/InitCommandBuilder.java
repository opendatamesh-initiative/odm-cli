package org.opendatamesh.cli.commands.local.init;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.init.InitFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.commands.local.LocalCommandBuilder.LOCAL_COMMAND;

@Component
public class InitCommandBuilder implements PicoCliCommandBuilder {

    @Autowired
    private InitFactory initFactory;
    @Autowired
    private OdmCliConfiguration configuration;

    private static final String INIT_COMMAND = "init";

    @Override
    public CommandLine buildCommand(String... args) {

        InitCommandExecutor executor = new InitCommandExecutor(configuration, initFactory);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);

        spec.name(INIT_COMMAND);
        spec.mixinStandardHelpOptions(true);
        spec.version("1.0.0");
        spec.usageMessage().description("Init a data descriptor json file");
        spec.usageMessage().sortOptions(false);

        handleWithOrder(Lists.newArrayList(
                order -> handleDescriptorDomainParam(executor, spec, order),
                order -> handleDescriptorNameParam(executor, spec, order),
                order -> handleDescriptorDisplayNameParam(executor, spec, order),
                order -> handleDescriptorVersionParam(executor, spec, order),
                order -> handleDescriptorFullyQualifiedNameParam(executor, spec, order),
                order -> handleDescriptorDescriptionParam(executor, spec, order),
                order -> handleDescriptorOwnerIdParam(executor, spec, order),
                order -> handleDescriptorOwnerNameParam(executor, spec, order),
                order -> handleOutputFileParam(executor, spec, order),
                order -> handleForceParam(executor, spec, order)
        ));
        return new CommandLine(spec);
    }

    @Override
    public String getParentCommandName() {
        return LOCAL_COMMAND;
    }

    @Override
    public String getCommandName() {
        return INIT_COMMAND;
    }

    private void handleDescriptorDomainParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorDomainOption = CommandLine.Model.OptionSpec
                .builder("--domain")
                .order(order)
                .description("Domain of the descriptor (required)")
                .paramLabel("domainName")
                .required(false)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("domain", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorDomainOption);
    }

    private void handleDescriptorNameParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorNameOption = CommandLine.Model.OptionSpec
                .builder("--name")
                .order(order)
                .description("Name of the descriptor (required)")
                .paramLabel("descriptorName")
                .required(false)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("name", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorNameOption);
    }

    private void handleDescriptorDisplayNameParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorDisplayNameOption = CommandLine.Model.OptionSpec
                .builder("--displayName")
                .order(order)
                .description("Display name of the descriptor")
                .paramLabel("displayName")
                .required(false)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("displayName", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorDisplayNameOption);
    }

    private void handleDescriptorVersionParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorVersionOption = CommandLine.Model.OptionSpec
                .builder("--descriptorVersion")
                .order(order)
                .description("Version of the descriptor")
                .paramLabel("version")
                .required(false)
                .defaultValue("1.0.0")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("descriptorVersion", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorVersionOption);
    }

    private void handleDescriptorFullyQualifiedNameParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorFullyQualifiedNameOption = CommandLine.Model.OptionSpec
                .builder("--fullyQualifiedName")
                .order(order)
                .description("Fully qualified name of the descriptor")
                .paramLabel("fullyQualifiedName")
                .required(false)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("fullyQualifiedName", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorFullyQualifiedNameOption);
    }

    private void handleDescriptorDescriptionParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorDescriptionOption = CommandLine.Model.OptionSpec
                .builder("--description")
                .order(order)
                .description("Description of the descriptor")
                .paramLabel("description")
                .required(false)
                .defaultValue("{description}")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("description", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorDescriptionOption);
    }

    private void handleDescriptorOwnerIdParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorOwnerIdOption = CommandLine.Model.OptionSpec
                .builder("--ownerId")
                .order(order)
                .description("Id of the owner of the descriptor")
                .paramLabel("ownerId")
                .required(false)
                .defaultValue(configuration.getTemplate().getOwner().getId())
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("ownerId", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorOwnerIdOption);
    }

    private void handleDescriptorOwnerNameParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec descriptorOwnerNameOption = CommandLine.Model.OptionSpec
                .builder("--ownerName")
                .order(order)
                .description("Name of the owner of the descriptor")
                .paramLabel("ownerName")
                .required(false)
                .defaultValue(configuration.getTemplate().getOwner().getName())
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("ownerName", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorOwnerNameOption);
    }

    private void handleOutputFileParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec outputFileTypeOption = CommandLine.Model.OptionSpec
                .builder("--outputFile")
                .order(order)
                .description("Sets the output file")
                .paramLabel("filename")
                .required(false)
                .defaultValue(Paths.get("").toAbsolutePath() + "/data-product-descriptor.json")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setImportSchemaCommandParam("outputFile", (String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(outputFileTypeOption);
    }

    private void handleForceParam(InitCommandExecutor executor, CommandLine.Model.CommandSpec spec, int order) {
        CommandLine.Model.OptionSpec outputFileTypeOption = CommandLine.Model.OptionSpec
                .builder("--force")
                .order(order)
                .description("Force file overwrite")
                .required(false)
                .type(boolean.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        if (Boolean.TRUE.equals(value))
                            executor.setImportSchemaCommandParam("force", "true");
                        return value;
                    }
                })
                .build();
        spec.addOption(outputFileTypeOption);
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

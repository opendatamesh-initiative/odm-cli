package org.opendatamesh.cli.commands.local.importschema;

import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.commands.local.LocalCommandBuilder;
import org.opendatamesh.cli.usecases.importschema.ImportSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

@Component
public class ImportCommandBuilder implements PicoCliCommandBuilder {

    @Autowired
    private ImportSchemaFactory importSchemaFactory;

    private static final String IMPORT_COMMAND = "import";

    @Override
    public CommandLine buildCommand() {
        ImportCommandExecutor executor = new ImportCommandExecutor(importSchemaFactory);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(IMPORT_COMMAND);
        spec.mixinStandardHelpOptions(true);
        spec.version("odm-cli local import 1.0.0");
        spec.usageMessage().description("Import schema into a descriptor file");

        handleDescriptorFilePathParam(executor, spec);
        handleFromParam(executor, spec);
        handleToParam(executor, spec);
        handleInParam(executor, spec);
        handleOutParam(executor, spec);

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

    private void handleDescriptorFilePathParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec) {
        CommandLine.Model.OptionSpec descriptorFilePathOption = CommandLine.Model.OptionSpec.builder("-f", "--file")
                .description("Name of the descriptor file (default: PATH/data-product-descriptor.json)")
                .paramLabel("FILE")
                .required(false)
                .defaultValue("./data-product-descriptor.json")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setDescriptorFilePath((String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(descriptorFilePathOption);
    }

    private void handleFromParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec) {
        CommandLine.Model.OptionSpec fromOption = CommandLine.Model.OptionSpec.builder("--from")
                .description("Import source type (ex. ddl, jdbc, unity, etc...)")
                .required(true)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setFrom((String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(fromOption);
    }

    private void handleToParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec) {
        CommandLine.Model.OptionSpec toOption = CommandLine.Model.OptionSpec.builder("--to")
                .description("Import target type (ex. output-port, input-port, api, schema, etc...)")
                .required(true)
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setTo((String) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(toOption);
    }

    private void handleInParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec) {
        CommandLine.Model.OptionSpec inParamOption = CommandLine.Model.OptionSpec.builder("--in-param")
                .description("Parameter related to source (ex. source file)")
                .arity("0..*")  // 0 or more values
                .paramLabel("<KEY=VALUE>")
                .type(List.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setInParams((List<String>) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(inParamOption);
    }

    private void handleOutParam(ImportCommandExecutor executor, CommandLine.Model.CommandSpec spec) {
        CommandLine.Model.OptionSpec outParamOption = CommandLine.Model.OptionSpec.builder("--out-param")
                .description("Parameter related to target (ex. target's name)")
                .arity("0..*")  // 0 or more values
                .paramLabel("<KEY=VALUE>")
                .type(List.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setOutParams((List<String>) value);
                        return value;
                    }
                })
                .build();
        spec.addOption(outParamOption);
    }

}

package org.opendatamesh.cli.commands.local.importschema;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.usecases.importschema.ImportSchemaFactory;
import org.opendatamesh.cli.utils.CommandOptionsUtils;

import java.util.HashMap;
import java.util.Map;

public class ImportCommandExecutor extends PicoCliCommandExecutor {

    private final ImportSchemaFactory importSchemaFactory;
    private final ImportSchemaExtension importSchemaExtension;

    private String descriptorFilePath;
    private Map<String, String> importSchemaCommandParams = new HashMap<>();

    public ImportCommandExecutor(ImportSchemaFactory importSchemaFactory, ImportSchemaExtension importSchemaExtension) {
        this.importSchemaFactory = importSchemaFactory;
        this.importSchemaExtension = importSchemaExtension;
    }

    @Override
    protected void handleRequiredOptions() {
        if (importSchemaExtension == null) {
            return;
        }
        CommandOptionsUtils.handleRequiredOptions(importSchemaExtension.getExtensionOptions());

    }

    @Override
    protected void executeUseCase() {
        if (importSchemaExtension == null) {
            return;
        }
        importSchemaFactory.getImportSchemaUseCase(
                descriptorFilePath,
                importSchemaCommandParams,
                importSchemaExtension
        ).execute();
    }

    public void setDescriptorFilePath(String descriptorFilePath) {
        this.descriptorFilePath = descriptorFilePath;
    }

    public void setImportSchemaCommandParam(String commandName, String commandValue) {
        if (commandValue == null) {
            return;
        }
        this.importSchemaCommandParams.put(commandName, commandValue);
    }
}

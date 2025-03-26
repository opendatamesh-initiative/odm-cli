package org.opendatamesh.cli.commands.local.init;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.init.InitFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitCommandExecutor extends PicoCliCommandExecutor {

    private final InitFactory initFactory;

    private final Map<String, String> importSchemaCommandParams = new HashMap<>();

    public InitCommandExecutor(
            OdmCliConfiguration odmCliConfiguration,
            InitFactory initFactory
    ) {
        super(odmCliConfiguration);
        this.initFactory = initFactory;
    }

    @Override
    protected void handleRequiredOptions(Boolean interactive) {
        List<String> requiredOptionKeys = List.of("domain", "name");
        for (String requiredOptionKey : requiredOptionKeys) {
            if (!importSchemaCommandParams.containsKey(requiredOptionKey)) {
                if (interactive) {
                    String value = System.console().readLine(String.format("Enter value for %s: ", requiredOptionKey));
                    setImportSchemaCommandParam(requiredOptionKey, value);
                } else {
                    throw new IllegalStateException("Missing value for required parameter: " + requiredOptionKey);
                }
            }
        }
    }

    @Override
    protected void executeUseCase() {
        initFactory.getImportSchemaUseCase(
                importSchemaCommandParams
        ).execute();
    }

    public void setImportSchemaCommandParam(String commandName, String commandValue) {
        if (commandValue == null) {
            return;
        }
        this.importSchemaCommandParams.put(commandName, commandValue);
    }
}

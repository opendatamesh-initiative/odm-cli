package org.opendatamesh.cli.commands.local.importer;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.importer.PortImporterFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ImportCommandExecutor extends PicoCliCommandExecutor {

    private final PortImporterFactory portImporterFactory;
    private final ImporterExtension<?> importSchemaExtension;

    private String descriptorFilePath;
    private final Map<String, String> importSchemaCommandParams = new HashMap<>();

    public ImportCommandExecutor(
            OdmCliConfiguration odmCliConfiguration,
            PortImporterFactory portImporterFactory,
            ImporterExtension<?> importSchemaExtension
    ) {
        super(odmCliConfiguration);
        this.portImporterFactory = portImporterFactory;
        this.importSchemaExtension = importSchemaExtension;
    }

    @Override
    protected void handleRequiredOptions(Boolean interactive) {
        if (importSchemaExtension == null) {
            return;
        }
        for (ExtensionOption extensionOption : importSchemaExtension.getExtensionOptions()) {
            if (extensionOption.isRequired() &&
                    !StringUtils.hasText(extensionOption.getGetter().get())
            ) {
                String extensionName = extensionOption.getNames().stream().findFirst().orElse("").replace("-", "");
                if (interactive && extensionOption.isInteractive()) {
                    String value = System.console().readLine(
                            String.format("Enter value for %s (%s): ",
                                    extensionName,
                                    extensionOption.getDescription()
                            )
                    );
                    extensionOption.getSetter().accept(value);
                } else {
                    throw new IllegalStateException("Missing value for required parameter:" + extensionName);
                }
            }
        }

    }

    @Override
    protected void executeUseCase() {
        if (importSchemaExtension == null) {
            return;
        }
        portImporterFactory.getImportSchemaUseCase(
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

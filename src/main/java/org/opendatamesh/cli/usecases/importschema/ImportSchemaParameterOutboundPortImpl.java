package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaArguments;

import java.nio.file.Path;
import java.util.Map;

class ImportSchemaParameterOutboundPortImpl implements ImportSchemaParameterOutboundPort {

    private final OdmCliConfiguration odmCliBaseConfiguration;
    private final Path descriptorFilePath;
    private final Map<String, String> importSchemaCommandParams;

    ImportSchemaParameterOutboundPortImpl(
            OdmCliConfiguration odmCliBaseConfiguration,
            String descriptorRootFilePath,
            Map<String, String> importSchemaCommandParams
    ) {
        this.odmCliBaseConfiguration = odmCliBaseConfiguration;
        this.descriptorFilePath = Path.of(descriptorRootFilePath);
        this.importSchemaCommandParams = importSchemaCommandParams;
    }

    @Override
    public ImportSchemaArguments getImportSchemaArguments() {
        ImportSchemaArguments arguments = new ImportSchemaArguments();
        arguments.setRootDescriptorPath(this.descriptorFilePath);
        arguments.setOdmCliConfig(this.odmCliBaseConfiguration.getBaseConfiguration());
        arguments.setParentCommandOptions(this.importSchemaCommandParams);
        return arguments;
    }
}

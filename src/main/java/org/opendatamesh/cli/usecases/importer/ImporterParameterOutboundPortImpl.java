package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;

import java.nio.file.Path;
import java.util.Map;

class ImporterParameterOutboundPortImpl implements ImporterParameterOutboundPort {

    private final OdmCliConfiguration odmCliBaseConfiguration;
    private final Path descriptorFilePath;
    private final Map<String, String> importSchemaCommandParams;

    ImporterParameterOutboundPortImpl(
            OdmCliConfiguration odmCliBaseConfiguration,
            String descriptorRootFilePath,
            Map<String, String> importSchemaCommandParams
    ) {
        this.odmCliBaseConfiguration = odmCliBaseConfiguration;
        this.descriptorFilePath = Path.of(descriptorRootFilePath);
        this.importSchemaCommandParams = importSchemaCommandParams;
    }

    @Override
    public ImporterArguments getImporterArguments() {
        ImporterArguments arguments = new ImporterArguments();
        arguments.setRootDescriptorPath(this.descriptorFilePath);
        arguments.setOdmCliConfig(this.odmCliBaseConfiguration.getBaseConfiguration());
        arguments.setParentCommandOptions(this.importSchemaCommandParams);
        return arguments;
    }
}

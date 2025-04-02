package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import java.util.Map;

class DataProductDescriptorInitializerParameterOutboundPortImpl implements DataProductDescriptorInitializerParameterOutboundPort {

    private final OdmCliConfiguration odmCliBaseConfiguration;
    private final Map<String, String> importSchemaCommandParams;

    DataProductDescriptorInitializerParameterOutboundPortImpl(
            OdmCliConfiguration odmCliBaseConfiguration,
            Map<String, String> importSchemaCommandParams
    ) {
        this.odmCliBaseConfiguration = odmCliBaseConfiguration;
        this.importSchemaCommandParams = importSchemaCommandParams;
    }

    @Override
    public ImporterArguments getImporterArguments() {
        ImporterArguments arguments = new ImporterArguments();
        arguments.setOdmCliConfig(this.odmCliBaseConfiguration.getBaseConfiguration());
        arguments.setParentCommandOptions(this.importSchemaCommandParams);
        return arguments;
    }
}

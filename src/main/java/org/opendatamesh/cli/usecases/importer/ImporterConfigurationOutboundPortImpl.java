package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;

class ImporterConfigurationOutboundPortImpl implements ImporterConfigurationOutboundPort {

    private final OdmCliConfiguration odmCliConfiguration;

    ImporterConfigurationOutboundPortImpl(OdmCliConfiguration odmCliConfiguration) {
        this.odmCliConfiguration = odmCliConfiguration;
    }

    @Override
    public OdmCliBaseConfiguration getOdmCliConfig() {
        return odmCliConfiguration.getBaseConfiguration();
    }
}

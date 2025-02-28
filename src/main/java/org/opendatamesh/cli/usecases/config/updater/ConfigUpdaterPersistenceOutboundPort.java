package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.databind.JsonNode;

interface ConfigUpdaterPersistenceOutboundPort {
    JsonNode getCurrentConfiguration();

    void saveConfiguration(JsonNode currentConfig);

}

package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.databind.JsonNode;

class ConfigUpdaterPersistenceOutboundPortMock implements ConfigUpdaterPersistenceOutboundPort {
    private final TestConfigUpdaterInitialState initialState;
    private final TestConfigUpdaterFinalState finalState;

    ConfigUpdaterPersistenceOutboundPortMock(TestConfigUpdaterInitialState initialState, TestConfigUpdaterFinalState finalState) {
        this.initialState = initialState;
        this.finalState = finalState;
    }

    @Override
    public JsonNode getCurrentConfiguration() {
        return initialState.getDefaultConfiguration().deepCopy();
    }

    @Override
    public void saveConfiguration(JsonNode currentConfig) {
        finalState.setFinalConfiguration(currentConfig);
    }
}

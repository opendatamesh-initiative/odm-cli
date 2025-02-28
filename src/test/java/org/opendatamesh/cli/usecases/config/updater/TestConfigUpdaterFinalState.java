package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.databind.JsonNode;

class TestConfigUpdaterFinalState {
    private JsonNode finalConfiguration;

    public JsonNode getFinalConfiguration() {
        return finalConfiguration;
    }

    public void setFinalConfiguration(JsonNode finalConfiguration) {
        this.finalConfiguration = finalConfiguration;
    }
}

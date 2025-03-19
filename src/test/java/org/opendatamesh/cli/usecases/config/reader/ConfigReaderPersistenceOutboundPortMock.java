package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



class ConfigReaderPersistenceOutboundPortMock implements ConfigReaderPersistenceOutboundPort {

    private final String rawConfig;

    public ConfigReaderPersistenceOutboundPortMock(String rawConfig) {
        this.rawConfig = rawConfig;
    }

    @Override
    public JsonNode getConfigContent() {
        try {
            return new ObjectMapper().readValue(rawConfig, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

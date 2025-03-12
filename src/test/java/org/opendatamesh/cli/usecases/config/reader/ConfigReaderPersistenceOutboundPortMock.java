package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.opendatamesh.cli.usecases.importer.referencehandler.utils.JacksonUtils.parserFixModule;

class ConfigReaderPersistenceOutboundPortMock implements ConfigReaderPersistenceOutboundPort {

    private final String rawConfig;

    public ConfigReaderPersistenceOutboundPortMock(String rawConfig) {
        this.rawConfig = rawConfig;
    }

    @Override
    public JsonNode getConfigContent() {
        try {
            return new ObjectMapper().registerModule(parserFixModule()).readValue(rawConfig, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

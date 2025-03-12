package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.opendatamesh.cli.usecases.importer.referencehandler.utils.JacksonUtils.parserFixModule;

class ConfigReaderPersistenceOutboundPortImpl implements ConfigReaderPersistenceOutboundPort {
    private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(parserFixModule());
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory()).registerModule(parserFixModule());
    private final Environment environment;
    private final ResourceLoader resourceLoader;

    ConfigReaderPersistenceOutboundPortImpl(Environment environment, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public JsonNode getConfigContent() {
        try {
            String springConfigRawContent = getRawStringContent();
            return isJson(springConfigRawContent) ?
                    jsonMapper.readTree(springConfigRawContent) :
                    yamlMapper.readTree(springConfigRawContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private String getRawStringContent() {
        String springAppJson = environment.getProperty("spring.application.json");
        if (springAppJson != null && !springAppJson.trim().isEmpty()) {
            return springAppJson;
        }
        Resource resource = resourceLoader.getResource("classpath:application.yaml");
        if (!resource.exists()) {
            resource = resourceLoader.getResource("classpath:application.yml");
        }
        if (!resource.exists()) {
            return "";
        }
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isJson(String input) {
        String trimmed = input.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }
}

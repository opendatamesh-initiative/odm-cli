package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


class ConfigUpdaterPersistenceOutboundPortImpl implements ConfigUpdaterPersistenceOutboundPort {

    private final Environment environment;
    private final ResourceLoader resourceLoader;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    ConfigUpdaterPersistenceOutboundPortImpl(Environment environment, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }


    @Override
    public JsonNode getCurrentConfiguration() {
        try {
            String springConfigRawContent = getRawStringContent();
            return isJson(springConfigRawContent) ?
                    jsonMapper.readTree(springConfigRawContent) :
                    yamlMapper.readTree(springConfigRawContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveConfiguration(JsonNode currentConfig) {
        try {
            String additionalLocation = environment.getProperty("spring.config.additional-location");
            if (additionalLocation == null) {
                throw new IllegalStateException("The external configuration file is not present.");
            }
            String filePath = additionalLocation.startsWith("file:")
                    ? additionalLocation.substring(5)
                    : additionalLocation;
            File configFile = new File(filePath);
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    throw new IOException("Failed to create external configuration file: " + filePath);
                }
            }
            yamlMapper.writeValue(configFile, currentConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error writing external configuration file", e);
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

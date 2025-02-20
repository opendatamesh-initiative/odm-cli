package org.opendatamesh.cli.configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This beans has "thread" scope. This allows its state to be accessed and modified
 * by other beans. This behaviour is used to overwrite default configuration values
 * by cli commands options and to allow a shared configuration context among all
 * Spring components.
 */
@Component
@ConfigurationProperties(prefix = "cli")
@Scope("thread")
public class OdmCliConfiguration {

    private Config cliConfiguration;
    private List<OdmCliBaseConfiguration.SystemConfig> systems;

    @Autowired
    private Environment environment;
    @Autowired
    private ResourceLoader resourceLoader;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public OdmCliBaseConfiguration getBaseConfiguration() {
        OdmCliBaseConfiguration baseConfiguration = new OdmCliBaseConfiguration();
        baseConfiguration.setCliConfiguration(getCliConfiguration());
        baseConfiguration.setSystems(getSystems());
        return baseConfiguration;
    }

    public Config getCliConfiguration() {
        return cliConfiguration;
    }

    public void setCliConfiguration(Config cliConfiguration) {
        this.cliConfiguration = cliConfiguration;
    }

    public List<OdmCliBaseConfiguration.SystemConfig> getSystems() {
        return systems;
    }

    public void setSystems(List<OdmCliBaseConfiguration.SystemConfig> systems) {
        this.systems = systems;
    }

    public static class Config extends OdmCliBaseConfiguration.Config {
        private String saveFormat;
        private Boolean interactive;

        public String getSaveFormat() {
            return Optional.ofNullable(saveFormat)
                    .map(String::toUpperCase)
                    .orElse(null);
        }

        public void setSaveFormat(String saveFormat) {
            this.saveFormat = saveFormat;
        }

        public Boolean isInteractive() {
            return interactive;
        }

        public void setInteractive(Boolean interactive) {
            this.interactive = interactive;
        }
    }

    public Map<String, String> getAllConfigurations() {
        String rawConfig = getConfigContent();
        return flattenJsonOrYaml(rawConfig);
    }

    private String getConfigContent() {
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

    private Map<String, String> flattenJsonOrYaml(String input) {
        try {
            JsonNode root = isJson(input) ? jsonMapper.readTree(input) : yamlMapper.readTree(input);
            Map<String, String> flatMap = new LinkedHashMap<>();
            flatten("", root, flatMap);
            return flatMap;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isJson(String input) {
        String trimmed = input.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private void flatten(String prefix, JsonNode node, Map<String, String> flatMap) {
        if (node.isValueNode()) {
            flatMap.put(prefix, node.asText());
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                String newKey = prefix.isEmpty() ? key : prefix + "." + key;
                flatten(newKey, entry.getValue(), flatMap);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newKey = prefix + "[" + i + "]";
                flatten(newKey, node.get(i), flatMap);
            }
        }
    }
}

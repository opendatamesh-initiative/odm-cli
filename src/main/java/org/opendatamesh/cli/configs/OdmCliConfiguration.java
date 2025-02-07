package org.opendatamesh.cli.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.util.*;

@ConfigurationProperties(prefix = "cli")
public class OdmCliConfiguration {

    private OdmCliBaseConfiguration.Config cliConfiguration;
    private List<OdmCliBaseConfiguration.System> remoteSystemsConfigurations;
    private String env;

    public OdmCliBaseConfiguration.Config getCliConfiguration() {
        return cliConfiguration;
    }

    public void setCliConfiguration(OdmCliBaseConfiguration.Config cliConfiguration) {
        this.cliConfiguration = cliConfiguration;
    }

    public List<OdmCliBaseConfiguration.System> getRemoteSystemsConfigurations() {
        return remoteSystemsConfigurations;
    }

    public void setRemoteSystemsConfigurations(List<OdmCliBaseConfiguration.System> remoteSystemsConfigurations) {
        this.remoteSystemsConfigurations = remoteSystemsConfigurations;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Map<String, String> getEnvAsMap() {
        try {
            if (env == null || env.isEmpty()) {
                return Collections.emptyMap();
            }
            return flattenJson(env);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private Map<String, String> flattenJson(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonString);
        Map<String, String> flatMap = new LinkedHashMap<>();
        flatten("", root, flatMap);
        return flatMap;
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

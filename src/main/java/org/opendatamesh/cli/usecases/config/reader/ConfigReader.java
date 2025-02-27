package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.usecases.UseCaseReturning;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class ConfigReader implements UseCaseReturning<Map<String, String>> {

    private final ConfigReaderPersistenceOutboundPort persistenceOutboundPort;

    ConfigReader(ConfigReaderPersistenceOutboundPort persistenceOutboundPort) {
        this.persistenceOutboundPort = persistenceOutboundPort;
    }

    @Override
    public Map<String, String> execute() {
        JsonNode configuration = persistenceOutboundPort.getConfigContent();
        return flatten(configuration);
    }

    private Map<String, String> flatten(JsonNode configuration) {
        Map<String, String> flatMap = new LinkedHashMap<>();
        flatten("", configuration, flatMap);
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

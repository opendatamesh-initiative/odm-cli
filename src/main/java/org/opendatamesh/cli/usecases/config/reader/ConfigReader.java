package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.utils.usecases.UseCase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class ConfigReader implements UseCase {

    private final ConfigReaderPersistenceOutboundPort persistenceOutboundPort;
    private final ConfigReaderPresenter presenter;

    ConfigReader(ConfigReaderPersistenceOutboundPort persistenceOutboundPort, ConfigReaderPresenter presenter) {
        this.persistenceOutboundPort = persistenceOutboundPort;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        try {
            JsonNode configuration = persistenceOutboundPort.getConfigContent();
            presenter.presentConfiguration(flatten(configuration));
        } catch (Exception e) {
            presenter.presentException(e);
        }
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

package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.util.CollectionUtils;

import java.util.*;

class ConfigUpdater implements UseCase {

    private final ConfigUpdaterPersistenceOutboundPort persistenceOutboundPort;
    private final ConfigUpdaterParametersOutboundPort parametersOutboundPort;

    ConfigUpdater(ConfigUpdaterPersistenceOutboundPort persistenceOutboundPort, ConfigUpdaterParametersOutboundPort parametersOutboundPort) {
        this.persistenceOutboundPort = persistenceOutboundPort;
        this.parametersOutboundPort = parametersOutboundPort;
    }

    @Override
    public void execute() {
        //Retrieve the current configuration (default + external)
        JsonNode currentConfig = persistenceOutboundPort.getCurrentConfiguration();
        handleSimpleFieldsDelete(currentConfig);
        handleArraysDelete(currentConfig);
        handleSimpleParametersAddition(currentConfig);
        handleArraysAddition(currentConfig);
        JsonNode patchToApplyOnDefaultConfig = computeDifferenceOnDefaultConfig(currentConfig);
        persistenceOutboundPort.saveConfiguration(patchToApplyOnDefaultConfig);
    }

    private void handleArraysAddition(JsonNode currentConfig) {
        Map<String, Map<String, String>> arraysEntriesToAdd = parametersOutboundPort.getArraysEntriesToAdd();
        arraysEntriesToAdd.forEach((arrayField, entries) -> {
            JsonPointer arrayFieldPointer = dotToJsonPointer(arrayField);
            JsonNode arrayFieldNode = currentConfig.at(arrayFieldPointer);
            //Init array if missing
            if (arrayFieldNode.isMissingNode()) {
                arrayFieldNode = JsonNodeFactory.instance.arrayNode();
                JsonNode parent = currentConfig.at(arrayFieldPointer.head());
                if (parent.isObject()) {
                    ((ObjectNode) parent).put(arrayFieldPointer.last().getMatchingProperty(), arrayFieldNode);
                } else {
                    throw new IllegalStateException("Impossible to add an array to a node that is not an Object: " + arrayFieldPointer);
                }
            }
            if (!CollectionUtils.isEmpty(entries)) {
                ObjectNode newEntry = JsonNodeFactory.instance.objectNode();
                entries.forEach((entryField, entryValue) -> {
                    JsonPointer entryFieldPointer = dotToJsonPointer(entryField);
                    ensureParentExists(newEntry, entryFieldPointer);
                    ((ObjectNode) newEntry.at(entryFieldPointer.head()))
                            .put(entryFieldPointer.last()
                                    .getMatchingProperty(), entryValue);
                });
                ((ArrayNode) arrayFieldNode).add(newEntry);
            }
        });
    }

    private void handleSimpleParametersAddition(JsonNode currentConfig) {
        Map<String, String> fieldsToAdd = parametersOutboundPort.getFieldsToAdd();
        fieldsToAdd.forEach((fieldKey, fieldValue) -> {
            JsonPointer jsonPointer = dotToJsonPointer(fieldKey);
            JsonNode parent = currentConfig.at(jsonPointer.head());
            if (parent.isMissingNode()) {
                ensureParentExists((ObjectNode) currentConfig, jsonPointer);
                parent = currentConfig.at(jsonPointer.head());
            }
            if (parent.isObject()) {
                ((ObjectNode) parent).put(jsonPointer.last().getMatchingProperty(), fieldValue);
            } else {
                throw new IllegalStateException("Impossible to add a simple parameter to a node that is not an Object: " + jsonPointer);
            }
        });
    }

    private void ensureParentExists(ObjectNode root, JsonPointer pointer) {
        ObjectNode current = root;
        for (JsonPointer p = pointer; p != null && p.getMatchingProperty() != null; p = p.tail()) {
            JsonNode nextNode = current.get(p.getMatchingProperty());
            if (nextNode == null || nextNode.isMissingNode() || !nextNode.isObject()) {
                current.set(p.getMatchingProperty(), JsonNodeFactory.instance.objectNode());
            }
            current = (ObjectNode) current.get(p.getMatchingProperty());
        }
    }

    private void handleArraysDelete(JsonNode currentConfig) {
        Map<String, Map<String, String>> arraysEntriesToDelete = parametersOutboundPort.getArraysEntriesToDelete();
        arraysEntriesToDelete.forEach((arrayField, entryToDelete) -> {
            JsonPointer jsonPointer = dotToJsonPointer(arrayField);
            JsonNode arrayNode = currentConfig.at(jsonPointer);
            if (arrayNode.isArray()) {
                List<JsonNode> filteredElements = new ArrayList<>();
                for (JsonNode arrayElement : arrayNode) {
                    if (!arrayElementMatchesWithEntry(entryToDelete, arrayElement)) {
                        filteredElements.add(arrayElement);
                    }
                }
                ((ArrayNode) arrayNode).removeAll();
                ((ArrayNode) arrayNode).addAll(filteredElements);
            } else {
                throw new IllegalStateException("Impossible to delete an element that is not part of an array: " + jsonPointer);
            }
        });
    }

    private boolean arrayElementMatchesWithEntry(Map<String, String> entryToDelete, JsonNode element) {
        return entryToDelete.entrySet().stream()
                .allMatch(entry -> {
                    JsonNode node = element.at(dotToJsonPointer(entry.getKey()));
                    return !node.isMissingNode() && node.asText().equals(entry.getValue());
                });
    }

    private void handleSimpleFieldsDelete(JsonNode currentConfig) {
        Set<String> fieldsToDelete = parametersOutboundPort.getFieldsToDelete();
        for (String fieldToDelete : fieldsToDelete) {
            JsonPointer jsonPointer = dotToJsonPointer(fieldToDelete);
            JsonPointer parentPointer = jsonPointer.head();
            String keyToRemove = jsonPointer.last().toString().substring(1); // Remove leading '/'
            JsonNode parent = currentConfig.at(parentPointer);
            if (parent.isObject()) {
                ObjectNode parentObject = (ObjectNode) parent;
                if (parentObject.has(keyToRemove)) {
                    parentObject.remove(keyToRemove);
                }
            } else {
                throw new IllegalStateException("Impossible to delete an element that has not an Object as parent: " + jsonPointer);
            }
        }
    }

    private JsonPointer dotToJsonPointer(String dotNotation) {
        return JsonPointer.compile("/" + dotNotation.replace(".", "/"));
    }


    private JsonNode computeDifferenceOnDefaultConfig(JsonNode updatedConfig) {
        JsonNode defaultConfig = persistenceOutboundPort.getCurrentConfiguration();
        return computeDeepDifference(updatedConfig, defaultConfig);
    }

    private JsonNode computeDeepDifference(JsonNode updatedConfig, JsonNode defaultConfig) {
        ObjectNode diff = new ObjectMapper().createObjectNode();

        // Check for fields in the updatedConfig that are not in/are different from defaultConfig
        Iterator<Map.Entry<String, JsonNode>> fields = updatedConfig.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode updatedValue = field.getValue();
            JsonNode defaultValue = defaultConfig.get(key);

            if (defaultValue == null) {
                diff.set(key, updatedValue);
            } else if (updatedValue.isObject() && defaultValue.isObject()) {
                JsonNode nestedDiff = computeDeepDifference(updatedValue, defaultValue);
                if (!nestedDiff.isEmpty()) {
                    diff.set(key, nestedDiff);
                }
            } else if (!updatedValue.equals(defaultValue)) {
                diff.set(key, updatedValue);
            }
        }

        // Check for fields in the defaultConfig that are not in updatedConfig
        Iterator<Map.Entry<String, JsonNode>> defaultFields = defaultConfig.fields();
        while (defaultFields.hasNext()) {
            Map.Entry<String, JsonNode> field = defaultFields.next();
            String key = field.getKey();
            if (!updatedConfig.has(key)) {
                diff.set(key, JsonNodeFactory.instance.nullNode());
            }
        }

        return diff;
    }
}

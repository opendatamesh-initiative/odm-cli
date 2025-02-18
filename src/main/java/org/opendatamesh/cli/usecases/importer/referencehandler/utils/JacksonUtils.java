package org.opendatamesh.cli.usecases.importer.referencehandler.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for handling JSON operations using Jackson.
 * <p>
 * This class provides static methods for working with JSON nodes.
 * </p>
 */
public abstract class JacksonUtils {

    /**
     * Merges two {@link JsonNode} objects deeply.
     * <p>
     * The fields present in the {@code newNode} are copied as-is.
     * Fields from the {@code originalNode} that are not present in the {@code newNode} are included as well.
     * In case of conflicts (i.e., when both nodes contain the same field), the value from {@code newNode} will take precedence.
     * Nested objects are merged recursively, and arrays are preserved as-is from the {@code newNode}.
     * </p>
     * <p>Example:</p>
     * <pre>
     * {@code
     * JsonNode newNode = {
     *   "field1": "value1",
     *   "field2": { "field21": "value21" },
     *   "field3": ["value3"]
     * };
     *
     * JsonNode originalNode = {
     *   "field1": "value2",
     *   "field2": { "field21": "value4",
     *               "field_original_1": "value"
     *             },
     *   "field_original_2": "value"
     *   "field3": ["anotherValue"]
     * };
     *
     * JsonNode mergedNode = mergeJsonNodes(objectMapper, originalNode, newNode);
     * //Output:
     * {
     *   "field1": "value1",
     *   "field2": { "field21": "value21",
     *               "field_original_1": "value"
     *             },
     *   "field_original_2": "value"
     *   "field3": ["value3"]
     * }
     * }
     * </pre>
     *
     * @param objectMapper the {@link ObjectMapper} used to create a new {@link ObjectNode} for merging.
     * @param originalNode the original JSON node.
     * @param newNode      the new JSON node.
     * @return a new {@link JsonNode} representing the merged result of the two input nodes.
     */
    public static JsonNode mergeJsonNodes(ObjectMapper objectMapper, JsonNode originalNode, JsonNode newNode) {
        if (newNode == null) {
            return originalNode;
        }
        if (newNode.isNull()) {
            return newNode;
        }
        if (originalNode == null || originalNode.isNull()) {
            return newNode;
        }
        if (newNode.isObject() && originalNode.isObject()) {
            ObjectNode mergedNode = objectMapper.createObjectNode();
            newNode.fieldNames().forEachRemaining(field -> mergedNode.set(field, mergeJsonNodes(objectMapper, originalNode.get(field), newNode.get(field))));
            originalNode.fieldNames().forEachRemaining(field -> mergedNode.set(field, mergeJsonNodes(objectMapper, originalNode.get(field), newNode.get(field))));
            return mergedNode;
        }

        if (newNode.isArray() && originalNode.isArray()) {
            return newNode;
        }

        return newNode;
    }


}

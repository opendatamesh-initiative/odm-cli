package org.opendatamesh.cli.usecases.importschema.referencehandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.dpds.model.core.ReferenceableEntityDPDS;
import org.opendatamesh.dpds.model.core.StandardDefinitionDPDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.opendatamesh.cli.usecases.importschema.referencehandler.utils.JacksonUtils.mergeJsonNodes;

/**
 * Utility class used inside the data product descriptor visitors implementations
 * to resolve references based on the {@link DescriptorFormat}
 */
public class ReferenceHandler {

    private final String REF = "$ref";
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final DescriptorFormat format;
    private final Path descriptorRootPath;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ReferenceHandler(DescriptorFormat format, Path descriptorRootPath) {
        this.format = format;
        this.descriptorRootPath = descriptorRootPath;
        this.jsonMapper = new ObjectMapper();
        configObjectMapper(jsonMapper);
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        configObjectMapper(yamlMapper);
    }

    public void handleReference(ReferenceableEntityDPDS referencableEntity) {
        if (referencableEntity == null) {
            return;
        }
        switch (format) {
            case CANONICAL:
                if (StringUtils.hasText(referencableEntity.getRef())) {
                    loadAttributesFromRef(referencableEntity.getRef(), referencableEntity);
                }
                break;
            case NORMALIZED:
                if (!StringUtils.hasText(referencableEntity.getRef())) {
                    break; //Ignores it
                }
                String ref = referencableEntity.getRef();
                storeEntityContent(ref, referencableEntity);
                clearEntityAttributes(referencableEntity);
                referencableEntity.setRef(ref);
                break;
        }

    }

    public void handleApiDefinitionReference(StandardDefinitionDPDS standardDefinition) {
        if (standardDefinition == null) {
            return;
        }
        switch (format) {
            case CANONICAL:
                if (standardDefinition.isDefinitionReference() &&
                        standardDefinition.getDefinitionReference().getRef() != null &&
                        isMediaTypeDeserializable(standardDefinition.getDefinitionReference().getMediaType())
                ) {
                    ObjectNode apiDefinition = JsonNodeFactory.instance.objectNode();
                    loadAttributesFromRef(standardDefinition.getDefinitionReference().getRef(), apiDefinition);
                    standardDefinition.setDefinition(apiDefinition);
                    break;
                }

                break;
            case NORMALIZED:
                if (standardDefinition.isDefinitionJson()) {
                    if (standardDefinition.getDefinitionJson().has(REF)) {
                        storeEntityContent(standardDefinition.getDefinitionJson().get(REF).asText(), standardDefinition.getDefinitionJson());
                        cleanAllApiDefinitionAttributeExceptRef(standardDefinition);
                    }
                    break;
                }
                if (standardDefinition.isDefinitionReference()) {
                    //Already normalized
                    break;
                }
        }
    }

    private void cleanAllApiDefinitionAttributeExceptRef(StandardDefinitionDPDS standardDefinition) {
        ObjectNode apiDefinition = JsonNodeFactory.instance.objectNode();
        apiDefinition.set(REF, standardDefinition.getDefinitionJson().get(REF));
        standardDefinition.setDefinition(apiDefinition);
    }

    private void storeEntityContent(String ref, Object entity) {
        File targetFile = resolveFile(ref);
        try {
            generateParentDirsIfNotExist(targetFile);

            if (targetFile.exists()) {
                String rawContent = Files.readString(targetFile.toPath(), StandardCharsets.UTF_8);
                JsonNode sourceFile = getMapper(ref).readTree(rawContent);
                //Using plain ObjectMapper so also null values are represented
                JsonNode pojoContent = new ObjectMapper().valueToTree(entity);
                JsonNode mergedContent = mergeJsonNodes(getMapper(ref), sourceFile, pojoContent);
                Object contentToSave = getMapper(ref).treeToValue(mergedContent, Object.class);
                getMapper(ref).writerWithDefaultPrettyPrinter().writeValue(targetFile, contentToSave);
            } else {
                getMapper(ref).writerWithDefaultPrettyPrinter().writeValue(targetFile, entity);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to store entity content in file: " + ref, e);
        }
    }

    private void generateParentDirsIfNotExist(File targetFile) {
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    private <T> void loadAttributesFromRef(String ref, T targetObject) {
        if (ref == null || ref.isEmpty()) {
            return;
        }
        try {
            File file = resolveFile(ref);
            if (!file.exists()) {
                logger.warn("Reference file not found: {}", ref);
            } else {
                getMapper(ref).readerForUpdating(targetObject).readValue(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load reference file: " + ref, e);
        }
    }

    private void clearEntityAttributes(Object entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    field.set(entity, null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to clear attribute: " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private File resolveFile(String ref) {
        if (descriptorRootPath == null) {
            throw new IllegalStateException("ReferenceHandler: missing descriptor root path");
        }
        return descriptorRootPath.getParent().resolve(ref).normalize().toFile();
    }

    private boolean isMediaTypeDeserializable(String mediaType) {
        return mediaType != null && (mediaType.toLowerCase().contains("json") || mediaType.toLowerCase().contains("yaml") || mediaType.toLowerCase().contains("yml"));
    }


    private void configObjectMapper(ObjectMapper objectMapper) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findSerializer(Annotated am) {
                return null;
            }

            @Override
            public Object findDeserializer(Annotated am) {
                return null;
            }
        });
    }

    private ObjectMapper getMapper(String ref) {
        if (ref.endsWith(".json")) {
            return jsonMapper;
        } else if (ref.endsWith(".yaml") || ref.endsWith(".yml")) {
            return yamlMapper;
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + ref);
        }
    }
}

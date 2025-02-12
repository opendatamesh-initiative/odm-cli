package org.opendatamesh.cli.usecases.importschema.referencehandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.dpds.model.core.ReferenceableEntityDPDS;
import org.opendatamesh.dpds.model.core.StandardDefinitionDPDS;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class ReferenceHandler {

    private final String REF = "$ref";
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final DescriptorFormat format;
    private final Path descriptorRootPath;

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
                    //TODO CREATE ref (maybe it can be done by navigating from root to child)
                    break; //Now it ignores it
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
                    } else {
                        //TODO CREATE ref (maybe it can be done by navigating from root to child)
                    }
                    break;
                }
                if (standardDefinition.isDefinitionReference()) {
                    if (standardDefinition.getDefinitionReference().getRef() != null && standardDefinition.getDefinitionReference().getRawContent() != null) {
                        //TODO save the rawContent on a file based on the ref
                        break;
                    }
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
        //TODO abstract the retrieve of the file
        File targetFile = resolveFile(ref);

        try {
            if (ref.endsWith(".json")) {
                jsonMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, entity);
            } else if (ref.endsWith(".yaml") || ref.endsWith(".yml")) {
                yamlMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, entity);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + ref);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store entity content in file: " + ref, e);
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

    private <T> void loadAttributesFromRef(String ref, T targetObject) {
        if (ref == null || ref.isEmpty()) {
            return;
        }
        //TODO abstract the retrieve of the file
        File file = resolveFile(ref);
        if (!file.exists()) {
            throw new IllegalArgumentException("Reference file not found: " + ref);
        }

        try {
            if (ref.endsWith(".json")) {
                targetObject = jsonMapper.readerForUpdating(targetObject).readValue(file);
            } else if (ref.endsWith(".yaml") || ref.endsWith(".yml")) {
                yamlMapper.readerForUpdating(targetObject).readValue(file);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + ref);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load reference file: " + ref, e);
        }
    }

    private File resolveFile(String ref) {
        if (descriptorRootPath != null) {
            Path resolvedPath = descriptorRootPath.getParent().resolve(ref).normalize();
            return resolvedPath.toFile();
        }
        return new File(ref);
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
}

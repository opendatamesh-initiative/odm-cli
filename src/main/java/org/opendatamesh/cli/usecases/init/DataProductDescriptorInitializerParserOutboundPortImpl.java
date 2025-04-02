package org.opendatamesh.cli.usecases.init;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.dpds.model.DataProductVersion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


class DataProductDescriptorInitializerParserOutboundPortImpl implements DataProductDescriptorInitializerParserOutboundPort {

    private final OdmCliConfiguration odmCliConfiguration;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    DataProductDescriptorInitializerParserOutboundPortImpl(OdmCliConfiguration odmCliConfiguration) {
        this.odmCliConfiguration = odmCliConfiguration;
        this.jsonMapper = new ObjectMapper();
        configObjectMapper(jsonMapper);
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        configObjectMapper(yamlMapper);
    }

    @Override
    public DataProductVersion getDataProductVersion() {
        try {
            return new DataProductVersion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void saveObject(T object, String outputFilePath, boolean force) {
        if(isOutputFileCorrect(outputFilePath, force)) {
            Path objectFilePath = Path.of(outputFilePath);
            try {
                JsonNode objectContent = new ObjectMapper().valueToTree(object);
                Object contentToSave = getMapper(objectFilePath.toString()).treeToValue(objectContent, Object.class);
                getMapper(objectFilePath.toString()).writerWithDefaultPrettyPrinter().writeValue(objectFilePath.toFile(), contentToSave);
            } catch (IOException e) {
                throw new RuntimeException("Failed to descriptor in file: " + objectFilePath, e);
            }
        }
    }

    private boolean isOutputFileCorrect(String outputFilePath, boolean force) {

        if (!hasWritePermission(outputFilePath))
            return false;

        if (!hasValidExtension(outputFilePath, List.of("json", "yaml", "yml")))
            return false;

        if (force)
            return true;

        return askToOverwrite(outputFilePath);

    }

    private static boolean hasWritePermission(String filePath) {
        String expandedPath = filePath.replaceFirst("^~", System.getProperty("user.home"));
        File folder = new File(expandedPath).getAbsoluteFile().getParentFile();
        return folder.exists() && folder.isDirectory() && folder.canWrite();
    }

    public static boolean hasValidExtension(String filePath, List<String> validExtensions) {
        String fileExtension = getFileExtension(filePath);
        return validExtensions.contains(fileExtension);
    }

    private static String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return filePath.substring(lastIndex + 1).toLowerCase();
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean askToOverwrite(String filePath) {
        if (!fileExists(filePath)) {
            return true;
        }
        String userInput = System.console().readLine("File exists. Do you want to overwrite it? (yes/no)");
        return userInput.trim().equalsIgnoreCase("yes");
    }

    private void configObjectMapper(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.referencehandler.DescriptorFormat;
import org.opendatamesh.dpds.referencehandler.ReferenceResolver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.opendatamesh.dpds.referencehandler.utils.JacksonUtils.mergeJsonNodes;

class PortImporterParserOutboundPortImpl implements ImporterParserOutboundPort {

    private final String descriptorFilePath;
    private final OdmCliConfiguration odmCliConfiguration;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    PortImporterParserOutboundPortImpl(String descriptorFilePath, OdmCliConfiguration odmCliConfiguration) {
        this.descriptorFilePath = descriptorFilePath;
        this.odmCliConfiguration = odmCliConfiguration;
        this.jsonMapper = new ObjectMapper();
        configObjectMapper(jsonMapper);
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        configObjectMapper(yamlMapper);
    }

    @Override
    public DataProductVersion getDataProductVersion() throws FileNotFoundException {
        Path descriptorPath = descriptorPath();
        File descriptorPathFile = descriptorPath.toFile();
        if (!descriptorPathFile.exists() || descriptorPathFile.isFile()) {
            throw new FileNotFoundException("Descriptor file not found at path: " + descriptorPath.toAbsolutePath());
        }
        try {
            DataProductVersion dataProductVersion = getMapper(descriptorPath.toString()).readValue(descriptorPathFile, DataProductVersion.class);
            ReferenceResolver.resolveReferences(DescriptorFormat.CANONICAL, dataProductVersion, descriptorPath);
            return dataProductVersion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDescriptor(DataProductVersion descriptor) {
        Path descriptorPath = descriptorPath();
        try {
            DescriptorFormat saveFormat = DescriptorFormat.valueOf(StringUtils.capitalize(odmCliConfiguration.getCliConfiguration().getSaveFormat()));
            ReferenceResolver.resolveReferences(saveFormat, descriptor, descriptorPath);

            if (descriptorPath.toFile().exists()) {
                String rawContent = Files.readString(descriptorPath, StandardCharsets.UTF_8);
                JsonNode sourceDescriptorFile = getMapper(descriptorPath.toString()).readTree(rawContent);
                //Using plain ObjectMapper so also null values are represented
                JsonNode descriptorContent = new ObjectMapper().valueToTree(descriptor);
                JsonNode mergedContent = mergeJsonNodes(getMapper(descriptorPath.toString()), sourceDescriptorFile, descriptorContent);
                Object contentToSave = getMapper(descriptorPath.toString()).treeToValue(mergedContent, Object.class);
                getMapper(descriptorPath.toString()).writerWithDefaultPrettyPrinter().writeValue(descriptorPath.toFile(), contentToSave);
            } else {
                getMapper(descriptorPath.toString()).writerWithDefaultPrettyPrinter().writeValue(descriptorPath.toFile(), descriptor);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to descriptor in file: " + descriptorPath.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean descriptorExists() {
        return Files.exists(descriptorPath());
    }

    @Override
    public boolean isDescriptorDirectory() {
        return Files.isDirectory(descriptorPath());
    }

    @Override
    public boolean isDescriptorEmpty() {
        try {
            return Files.size(descriptorPath()) == 0;
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to read the descriptor file: " + ex.getMessage(), ex);
        }
    }

    private Path descriptorPath() {
        try {
            return Paths.get(descriptorFilePath);
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException("Invalid descriptor root path: " + ex.getMessage(), ex);
        }
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

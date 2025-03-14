package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.importer.referencehandler.DescriptorFormat;
import org.opendatamesh.cli.usecases.importer.referencehandler.ReferenceResolver;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.opendatamesh.cli.usecases.importer.referencehandler.utils.JacksonUtils.mergeJsonNodes;
import static org.opendatamesh.cli.usecases.importer.referencehandler.utils.JacksonUtils.parserFixModule;

class PortPortImporterParserOutboundPortImpl implements PortImporterParserOutboundPort {

    private final Path descriptorPath;
    private final OdmCliConfiguration odmCliConfiguration;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    PortPortImporterParserOutboundPortImpl(Path descriptorPath, OdmCliConfiguration odmCliConfiguration) {
        this.descriptorPath = descriptorPath;
        this.odmCliConfiguration = odmCliConfiguration;
        this.jsonMapper = new ObjectMapper().registerModule(parserFixModule());
        configObjectMapper(jsonMapper);
        this.yamlMapper = new ObjectMapper(new YAMLFactory()).registerModule(parserFixModule());
        configObjectMapper(yamlMapper);
    }

    @Override
    public DataProductVersionDPDS getDataProductVersion() {
        try {
            DataProductVersionDPDS dataProductVersion = getMapper(descriptorPath.toString()).readValue(descriptorPath.toFile(), DataProductVersionDPDS.class);
            ReferenceResolver.resolveReferences(DescriptorFormat.CANONICAL, dataProductVersion, descriptorPath);
            return dataProductVersion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDescriptor(DataProductVersionDPDS descriptor) {
        try {
            DescriptorFormat saveFormat = DescriptorFormat.valueOf(StringUtils.capitalize(odmCliConfiguration.getCliConfiguration().getSaveFormat()));
            ReferenceResolver.resolveReferences(saveFormat, descriptor, descriptorPath);

            if (descriptorPath.toFile().exists()) {
                String rawContent = Files.readString(descriptorPath, StandardCharsets.UTF_8);
                JsonNode sourceDescriptorFile = getMapper(descriptorPath.toString()).readTree(rawContent);
                //Using plain ObjectMapper so also null values are represented
                JsonNode descriptorContent = new ObjectMapper().registerModule(parserFixModule()).valueToTree(descriptor);
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

    private void configObjectMapper(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
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

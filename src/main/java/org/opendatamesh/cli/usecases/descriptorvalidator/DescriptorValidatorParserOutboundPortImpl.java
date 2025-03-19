package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.referencehandler.DescriptorFormat;
import org.opendatamesh.dpds.referencehandler.ReferenceResolver;

import java.nio.file.Path;

class DescriptorValidatorParserOutboundPortImpl implements DescriptorValidatorParserOutboundPort {
    private final Path descriptorPath;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    DescriptorValidatorParserOutboundPortImpl(Path descriptorPath) {
        this.descriptorPath = descriptorPath;
        this.jsonMapper = new ObjectMapper();
        configObjectMapper(jsonMapper);
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        configObjectMapper(yamlMapper);
    }

    @Override
    public JsonNode getCanonicalRawDescriptor() {
        try {
            DataProductVersion dataProductVersion = getMapper(descriptorPath.toString()).readValue(descriptorPath.toFile(), DataProductVersion.class);
            ReferenceResolver.resolveReferences(DescriptorFormat.CANONICAL, dataProductVersion, descriptorPath);
            return jsonMapper.valueToTree(dataProductVersion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private void configObjectMapper(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }
}

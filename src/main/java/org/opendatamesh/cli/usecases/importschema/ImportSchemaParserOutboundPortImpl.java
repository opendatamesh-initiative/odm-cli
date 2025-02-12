package org.opendatamesh.cli.usecases.importschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.importschema.referencehandler.ReferenceHandler;
import org.opendatamesh.cli.usecases.importschema.referencehandler.DescriptorFormat;
import org.opendatamesh.cli.usecases.importschema.referencehandler.visitorsimpl.DataProductVersionRefVisitor;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.springframework.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

class ImportSchemaParserOutboundPortImpl implements ImportSchemaParserOutboundPort {

    private final Path descriptorPath;
    private final OdmCliConfiguration odmCliConfiguration;
    private final ObjectMapper objectMapper;

    ImportSchemaParserOutboundPortImpl(Path descriptorPath, OdmCliConfiguration odmCliConfiguration) {
        this.descriptorPath = descriptorPath;
        this.odmCliConfiguration = odmCliConfiguration;
        this.objectMapper = configObjectMapper();
    }

    @Override
    public DataProductVersionDPDS getDataProductVersion() {
        try {
            DataProductVersionDPDS dataProductVersion = objectMapper.readValue(descriptorPath.toFile(), DataProductVersionDPDS.class);
            resolveReferences(DescriptorFormat.CANONICAL, dataProductVersion);
            return dataProductVersion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDescriptor(DataProductVersionDPDS descriptor) {
        try (FileWriter writer = new FileWriter(descriptorPath.toFile())) {
            DescriptorFormat saveFormat = DescriptorFormat.valueOf(StringUtils.capitalize(odmCliConfiguration.getCliConfiguration().getSaveFormat()));
            resolveReferences(saveFormat, descriptor);
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(descriptor));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resolveReferences(DescriptorFormat format, DataProductVersionDPDS dataProductVersion) {
        ReferenceHandler referenceHandler = new ReferenceHandler(format, descriptorPath);
        DataProductVersionRefVisitor visitor = new DataProductVersionRefVisitor(referenceHandler);
        if (dataProductVersion.getInterfaceComponents() != null) {
            visitor.visit(dataProductVersion.getInterfaceComponents());
        }
        if (dataProductVersion.getInternalComponents() != null) {
            visitor.visit(dataProductVersion.getInternalComponents());
        }
        if (dataProductVersion.getComponents() != null) {
            visitor.visit(dataProductVersion.getComponents());
        }
    }

    private ObjectMapper configObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
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
        return mapper;
    }
}

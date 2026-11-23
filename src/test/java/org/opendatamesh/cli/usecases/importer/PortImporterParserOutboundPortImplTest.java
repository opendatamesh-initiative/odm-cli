package org.opendatamesh.cli.usecases.importer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PortImporterParserOutboundPortImplTest {

    @Autowired
    private OdmCliConfiguration configuration;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerializationOnEmptyFields() throws URISyntaxException, IOException {

        Path emptySerializationFilePath = Paths.get(getClass().getResource("test_empty_serialization.json").toURI());
        EmptySerializationExtensionMock importSchemaExtensionMock = new EmptySerializationExtensionMock();
        DataProductVersion emptyFieldsDataProductVersion = importSchemaExtensionMock.importElement(null, null);
        PortImporterParserOutboundPortImpl parserOutboundPort = new PortImporterParserOutboundPortImpl(
            emptySerializationFilePath.toString(), configuration
        );

        parserOutboundPort.saveDescriptor(emptyFieldsDataProductVersion);

        File outputFile = emptySerializationFilePath.toFile();
        JsonNode json = objectMapper.readTree(outputFile);

        assertTrue(json.has("info"), "Missing 'info' field");
        JsonNode infoNode = json.get("info");

        assertTrue(infoNode.has("contactPoints"), "Missing 'contactPoints' field");
        assertTrue(infoNode.get("contactPoints").isArray(), "'contactPoints' should be an array");
        assertEquals(0, infoNode.get("contactPoints").size(), "'contactPoints' should be empty");

        assertTrue(infoNode.has("owner"), "Missing 'owner' field");
        
        JsonNode ownerNode = infoNode.get("owner");
        assertTrue(ownerNode.has("id"), "Missing 'owner.id' field");
        assertEquals("", ownerNode.get("id").asText(), "'owner.id' should be an empty string");
    }

    @Test
    public void testReadExistingDescriptor(@TempDir Path tempDir) throws IOException {
        Path descriptorPath = tempDir.resolve("data-product-descriptor.json");
        Files.writeString(descriptorPath, "{\"dataProductDescriptor\":\"1.0.0\",\"info\":{\"name\":\"testName\",\"version\":\"1.0.0\"}}");
        PortImporterParserOutboundPortImpl parserOutboundPort = new PortImporterParserOutboundPortImpl(descriptorPath.toString(), configuration);

        DataProductVersion dataProductVersion = parserOutboundPort.getDataProductVersion();

        assertEquals("testName", dataProductVersion.getInfo().getName());
    }

    @Test
    public void testReadMissingDescriptor(@TempDir Path tempDir) {
        PortImporterParserOutboundPortImpl parserOutboundPort = new PortImporterParserOutboundPortImpl(tempDir.resolve("nope.json").toString(), configuration);

        assertThrows(FileNotFoundException.class, parserOutboundPort::getDataProductVersion);
    }
}

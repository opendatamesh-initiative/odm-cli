package org.opendatamesh.cli.usecases.importer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EmptySerializationTest {

    @Autowired
    private OdmCliConfiguration configuration;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerializationOnEmptyFields() throws URISyntaxException, IOException {

        EmptySerializationExtensionMock importSchemaExtensionMock = new EmptySerializationExtensionMock();

        ImporterParserMockState parserState = new ImporterParserMockState();
        parserState.setDataProductVersion(importSchemaExtensionMock.importElement(null, null));

        PortImporterParserOutboundPortImpl parserOutboundPort = new PortImporterParserOutboundPortImpl(
            Paths.get(getClass().getResource("test_empty_serialization.json").toURI()), configuration
        );

        ImporterArguments importSchemaArguments = new ImporterArguments();
        importSchemaArguments.setParentCommandOptions(Map.of(
                "to", "descriptor")
        );
        ImporterParameterOutboundPortMock parameterOutboundPort = new ImporterParameterOutboundPortMock(importSchemaArguments);

        DescriptorImporterExtensionMockState extensionState = new DescriptorImporterExtensionMockState();
        extensionState.setDataProductVersion(new DataProductVersion());
        extensionState.getDataProductVersion().setInfo(new Info());
        extensionState.getDataProductVersion().getInfo().setName("TestImportDescriptor");

        new DescriptorImporter(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
                .execute();

        File outputFile = Paths.get(getClass().getResource("test_empty_serialization.json").toURI()).toFile();
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
}

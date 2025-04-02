package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class PortImporterTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    @Test
    public void testImportSchema() throws IOException {
        ImporterParserMockState parserState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_parser_initial_state.json")
                ),
                ImporterParserMockState.class
        );
        ImporterParserOutboundPortMock parserOutboundPort = new ImporterParserOutboundPortMock(parserState);
        ImporterArguments importSchemaArguments = new ImporterArguments();
        importSchemaArguments.setParentCommandOptions(Map.of(
                "to", "output-port",
                "target", "output_port_name")
        );
        ImporterParameterOutboundPortMock parameterOutboundPort = new ImporterParameterOutboundPortMock(importSchemaArguments);

        PortImporterExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_extension_initial_state.json")
                ),
                PortImporterExtensionMockState.class
        );
        PortImporterExtensionMock importSchemaExtensionMock = new PortImporterExtensionMock(extensionState);

        new PortImporter(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
                .execute();


        ImporterParserMockState parserExpectedState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_parser_expected_final_state.json")
                ),
                ImporterParserMockState.class
        );

        assertThat(parserState)
                .usingRecursiveComparison()
                .isEqualTo(parserExpectedState);
    }

    @Test
    public void testImportSchemaWithPreExistingPorts() throws IOException {
        ImporterParserMockState parserState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_parser_initial_state.json")
                ),
                ImporterParserMockState.class
        );
        ImporterParserOutboundPortMock parserOutboundPort = new ImporterParserOutboundPortMock(parserState);
        ImporterArguments importSchemaArguments = new ImporterArguments();
        importSchemaArguments.setParentCommandOptions(Map.of(
                "to", "output-port",
                "target", "another_output_port_name")
        );

        ImporterParameterOutboundPortMock parameterOutboundPort = new ImporterParameterOutboundPortMock(importSchemaArguments);

        PortImporterExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_extension_initial_state.json")
                ),
                PortImporterExtensionMockState.class
        );
        PortImporterExtensionMock importSchemaExtensionMock = new PortImporterExtensionMock(extensionState);

        new PortImporter(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
                .execute();


        ImporterParserMockState parserExpectedState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_parser_expected_final_state.json")
                ),
                ImporterParserMockState.class
        );

        assertThat(parserState)
                .usingRecursiveComparison()
                .isEqualTo(parserExpectedState);
    }

}

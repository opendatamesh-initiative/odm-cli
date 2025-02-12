package org.opendatamesh.cli.usecases.importschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaArguments;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImportSchema {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testImportSchema() throws IOException {
        ImportSchemaParserMockState parserState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_parser_initial_state.json")
                ),
                ImportSchemaParserMockState.class
        );
        ImportSchemaParserOutboundPortMock parserOutboundPort = new ImportSchemaParserOutboundPortMock(parserState);
        ImportSchemaArguments importSchemaArguments = new ImportSchemaArguments();
        importSchemaArguments.setParentCommandOptions(Map.of("target", "output-port"));
        ImportSchemaParameterOutboundPortMock parameterOutboundPort = new ImportSchemaParameterOutboundPortMock(importSchemaArguments);

        ImportSchemaExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_extension_initial_state.json")
                ),
                ImportSchemaExtensionMockState.class
        );
        ImportSchemaExtensionMock importSchemaExtensionMock = new ImportSchemaExtensionMock(extensionState);

        new ImportSchema(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
                .execute();


        ImportSchemaParserMockState parserExpectedState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_parser_expected_final_state.json")
                ),
                ImportSchemaParserMockState.class
        );

        assertThat(parserState)
                .usingRecursiveComparison()
                .isEqualTo(parserExpectedState);
    }

    @Test
    public void testImportSchemaWithPreExistingPorts() throws IOException {
        ImportSchemaParserMockState parserState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_parser_initial_state.json")
                ),
                ImportSchemaParserMockState.class
        );
        ImportSchemaParserOutboundPortMock parserOutboundPort = new ImportSchemaParserOutboundPortMock(parserState);
        ImportSchemaArguments importSchemaArguments = new ImportSchemaArguments();
        importSchemaArguments.setParentCommandOptions(Map.of("target", "output-port"));
        ImportSchemaParameterOutboundPortMock parameterOutboundPort = new ImportSchemaParameterOutboundPortMock(importSchemaArguments);

        ImportSchemaExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_extension_initial_state.json")
                ),
                ImportSchemaExtensionMockState.class
        );
        ImportSchemaExtensionMock importSchemaExtensionMock = new ImportSchemaExtensionMock(extensionState);

        new ImportSchema(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
                .execute();


        ImportSchemaParserMockState parserExpectedState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_parser_expected_final_state.json")
                ),
                ImportSchemaParserMockState.class
        );

        assertThat(parserState)
                .usingRecursiveComparison()
                .isEqualTo(parserExpectedState);
    }
}

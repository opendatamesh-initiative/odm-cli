package org.opendatamesh.cli.usecases.importschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImportSchema {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testImportSchema() throws IOException {
        ImportSchemaParserMockState parserState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_parser_initial_state.json")
                ),
                ImportSchemaParserMockState.class
        );
        ImportSchemaParserOutboundPortMock parserOutboundPort = new ImportSchemaParserOutboundPortMock(parserState);
        ImportSchemaParameterOutboundPortMock parameterOutboundPort = new ImportSchemaParameterOutboundPortMock();

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
}

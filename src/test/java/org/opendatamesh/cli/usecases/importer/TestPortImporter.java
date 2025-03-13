package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opendatamesh.cli.usecases.importer.referencehandler.utils.JacksonUtils.parserFixModule;

public class TestPortImporter {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(parserFixModule());

    @Test
    public void testImportSchema() throws IOException {
        ImportSchemaParserMockState parserState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_parser_initial_state.json")
                ),
                ImportSchemaParserMockState.class
        );
        PortImporterParserOutboundPortMock parserOutboundPort = new PortImporterParserOutboundPortMock(parserState);
        ImporterArguments importSchemaArguments = new ImporterArguments();
        importSchemaArguments.setParentCommandOptions(Map.of(
                "to", "output-port",
                "target", "output_port_name")
        );        
        PortImporterParameterOutboundPortMock parameterOutboundPort = new PortImporterParameterOutboundPortMock(importSchemaArguments);

        ImporterExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_extension_initial_state.json")
                ),
                ImporterExtensionMockState.class
        );
        ImporterExtensionMock importSchemaExtensionMock = new ImporterExtensionMock(extensionState);

        new PortImporter(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
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
        PortImporterParserOutboundPortMock parserOutboundPort = new PortImporterParserOutboundPortMock(parserState);
        ImporterArguments importSchemaArguments = new ImporterArguments();
        importSchemaArguments.setParentCommandOptions(Map.of(
                "to", "output-port",
                "target", "another_output_port_name")
        );

        PortImporterParameterOutboundPortMock parameterOutboundPort = new PortImporterParameterOutboundPortMock(importSchemaArguments);

        ImporterExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_extension_initial_state.json")
                ),
                ImporterExtensionMockState.class
        );
        ImporterExtensionMock importSchemaExtensionMock = new ImporterExtensionMock(extensionState);

        new PortImporter(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
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

package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import org.opendatamesh.cli.exceptions.NotFoundException;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.dpds.model.interfaces.Port;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


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
        ImporterCommand command = new ImporterCommand(
                "data-product-descriptor.json",
                "output-port",
                "output_port_name",
                Map.of("to", "output-port", "target", "output_port_name")
        );

        PortImporterExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_extension_initial_state.json")
                ),
                PortImporterExtensionMockState.class
        );
        PortImporterExtensionMock importSchemaExtensionMock = new PortImporterExtensionMock(extensionState);

        new PortImporter(command, descriptor -> { }, () -> null, parserOutboundPort, importSchemaExtensionMock)
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
        ImporterCommand command = new ImporterCommand(
                "data-product-descriptor.json",
                "output-port",
                "another_output_port_name",
                Map.of("to", "output-port", "target", "another_output_port_name")
        );

        PortImporterExtensionMockState extensionState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_import_schema_preexistingports_extension_initial_state.json")
                ),
                PortImporterExtensionMockState.class
        );
        PortImporterExtensionMock importSchemaExtensionMock = new PortImporterExtensionMock(extensionState);

        new PortImporter(command, descriptor -> { }, () -> null, parserOutboundPort, importSchemaExtensionMock)
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

    @Test
    @SuppressWarnings("unchecked")
    public void testMissingDescriptorIsPresentedAsNotFound() throws IOException {
        ImporterParserOutboundPort parserOutboundPort = mock(ImporterParserOutboundPort.class);
        when(parserOutboundPort.descriptorExists()).thenReturn(false);
        ImporterExtension<Port> importerExtension = mock(ImporterExtension.class);
        ImporterPresenter presenter = mock(ImporterPresenter.class, CALLS_REAL_METHODS);
        ImporterCommand command = new ImporterCommand(
                "nope.json",
                "output-port",
                "output_port_name",
                Map.of("to", "output-port", "target", "output_port_name")
        );

        PortImporter portImporter = new PortImporter(command, presenter, () -> null, parserOutboundPort, importerExtension);

        assertThatThrownBy(portImporter::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Descriptor root path does not exist: nope.json");
        verify(parserOutboundPort, never()).getDataProductVersion();
        verify(parserOutboundPort, never()).saveDescriptor(any());
        verifyNoInteractions(importerExtension);
        verify(presenter, never()).presentDescriptorImported(any());
    }
}

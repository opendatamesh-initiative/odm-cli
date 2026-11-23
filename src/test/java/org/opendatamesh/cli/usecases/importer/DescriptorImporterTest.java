package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.info.Info;

import org.mockito.ArgumentCaptor;
import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DescriptorImporterTest {

    @Test
    public void testImportDescriptor() {
        ImporterParserMockState parserState = new ImporterParserMockState();
        parserState.setDataProductVersion(new DataProductVersion());

        ImporterParserOutboundPortMock parserOutboundPort = new ImporterParserOutboundPortMock(parserState);
        ImporterCommand command = new ImporterCommand(
                "data-product-descriptor.json",
                "descriptor",
                null,
                Map.of("to", "descriptor")
        );

        DescriptorImporterExtensionMockState extensionState = new DescriptorImporterExtensionMockState();
        extensionState.setDataProductVersion(new DataProductVersion());
        extensionState.getDataProductVersion().setInfo(new Info());
        extensionState.getDataProductVersion().getInfo().setName("TestImportDescriptor");

        DescriptorImporterExtensionMock importSchemaExtensionMock = new DescriptorImporterExtensionMock(extensionState);

        new DescriptorImporter(command, descriptor -> { }, () -> null, parserOutboundPort, importSchemaExtensionMock)
                .execute();

        ImporterParserMockState parserExpectedState = new ImporterParserMockState();
        parserExpectedState.setDataProductVersion(new DataProductVersion());
        parserExpectedState.getDataProductVersion().setInfo(new Info());
        parserExpectedState.getDataProductVersion().getInfo().setName("TestImportDescriptor");

        assertThat(parserState)
                .usingRecursiveComparison()
                .isEqualTo(parserExpectedState);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testImporterArgumentsAreBuiltFromCommand() {
        DataProductVersion existingDescriptor = new DataProductVersion();
        ImporterParserMockState parserState = new ImporterParserMockState();
        parserState.setDataProductVersion(existingDescriptor);
        ImporterParserOutboundPortMock parserOutboundPort = new ImporterParserOutboundPortMock(parserState);
        OdmCliBaseConfiguration odmCliConfig = new OdmCliBaseConfiguration();
        Map<String, String> options = Map.of("to", "descriptor", "from", "starter", "source", "starter");
        ImporterCommand command = new ImporterCommand("data-product-descriptor.json", "descriptor", null, options);

        ImporterExtension<DataProductVersion> importerExtension = mock(ImporterExtension.class);
        when(importerExtension.importElement(any(), any())).thenReturn(new DataProductVersion());

        new DescriptorImporter(command, descriptor -> { }, () -> odmCliConfig, parserOutboundPort, importerExtension)
                .execute();

        ArgumentCaptor<ImporterArguments> argumentsCaptor = ArgumentCaptor.forClass(ImporterArguments.class);
        verify(importerExtension).importElement(any(), argumentsCaptor.capture());
        ImporterArguments arguments = argumentsCaptor.getValue();
        assertThat(arguments.getRootDescriptorPath()).isEqualTo(Path.of("data-product-descriptor.json"));
        assertThat(arguments.getOdmCliConfig()).isSameAs(odmCliConfig);
        assertThat(arguments.getParentCommandOptions()).isEqualTo(options);
        assertThat(arguments.getDataProductVersion()).isSameAs(existingDescriptor);
    }
}

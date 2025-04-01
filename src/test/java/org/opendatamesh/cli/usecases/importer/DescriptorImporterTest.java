package org.opendatamesh.cli.usecases.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.info.Info;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DescriptorImporterTest {

    @Test
    public void testImportDescriptor() {
        ImporterParserMockState parserState = new ImporterParserMockState();
        parserState.setDataProductVersion(new DataProductVersion());

        ImporterParserOutboundPortMock parserOutboundPort = new ImporterParserOutboundPortMock(parserState);
        ImporterArguments importSchemaArguments = new ImporterArguments();
        importSchemaArguments.setParentCommandOptions(Map.of(
                "to", "descriptor")
        );
        ImporterParameterOutboundPortMock parameterOutboundPort = new ImporterParameterOutboundPortMock(importSchemaArguments);

        DescriptorImporterExtensionMockState extensionState = new DescriptorImporterExtensionMockState();
        extensionState.setDataProductVersion(new DataProductVersion());
        extensionState.getDataProductVersion().setInfo(new Info());
        extensionState.getDataProductVersion().getInfo().setName("TestImportDescriptor");

        DescriptorImporterExtensionMock importSchemaExtensionMock = new DescriptorImporterExtensionMock(extensionState);

        new DescriptorImporter(parameterOutboundPort, parserOutboundPort, importSchemaExtensionMock)
                .execute();

        ImporterParserMockState parserExpectedState = new ImporterParserMockState();
        parserExpectedState.setDataProductVersion(new DataProductVersion());
        parserExpectedState.getDataProductVersion().setInfo(new Info());
        parserExpectedState.getDataProductVersion().getInfo().setName("TestImportDescriptor");

        assertThat(parserState)
                .usingRecursiveComparison()
                .isEqualTo(parserExpectedState);
    }
}

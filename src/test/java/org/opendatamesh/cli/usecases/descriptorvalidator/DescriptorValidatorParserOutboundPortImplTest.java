package org.opendatamesh.cli.usecases.descriptorvalidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DescriptorValidatorParserOutboundPortImplTest {

    @TempDir
    Path tempDir;

    @Test
    public void testDescriptorFileChecks() throws IOException {
        Path emptyFile = Files.createFile(tempDir.resolve("empty.json"));
        Path descriptorFile = Files.writeString(tempDir.resolve("descriptor.json"), "{}");

        assertThat(new DescriptorValidatorParserOutboundPortImpl(tempDir.resolve("nope.json").toString()).descriptorExists()).isFalse();
        assertThat(new DescriptorValidatorParserOutboundPortImpl(tempDir.toString()).isDescriptorDirectory()).isTrue();
        assertThat(new DescriptorValidatorParserOutboundPortImpl(emptyFile.toString()).isDescriptorEmpty()).isTrue();

        DescriptorValidatorParserOutboundPortImpl parserOutboundPort = new DescriptorValidatorParserOutboundPortImpl(descriptorFile.toString());
        assertThat(parserOutboundPort.descriptorExists()).isTrue();
        assertThat(parserOutboundPort.isDescriptorDirectory()).isFalse();
        assertThat(parserOutboundPort.isDescriptorEmpty()).isFalse();
    }

    @Test
    public void testInvalidDescriptorPath() {
        DescriptorValidatorParserOutboundPortImpl parserOutboundPort = new DescriptorValidatorParserOutboundPortImpl("invalid\0path.json");

        assertThatThrownBy(parserOutboundPort::descriptorExists)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid descriptor root path: ");
    }
}

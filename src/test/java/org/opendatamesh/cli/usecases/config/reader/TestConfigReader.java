package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TestConfigReader {
    @Test
    public void testConfigReader() throws IOException {
        ConfigReaderPersistenceOutboundPort persistenceOutboundPort = new ConfigReaderPersistenceOutboundPortMock(
                Files.readString(Path.of(getClass().getResource("test_config_reader_raw_config.json").getPath()), StandardCharsets.UTF_8)
        );

        Map<String, String> result = new ConfigReader(persistenceOutboundPort).execute();
        Map<String, String> expectedResult = new ObjectMapper().readValue(new File(getClass().getResource("test_config_reader_expected_result.json").getPath()), Map.class);
        Assertions.assertThat(result).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedResult);
    }
}

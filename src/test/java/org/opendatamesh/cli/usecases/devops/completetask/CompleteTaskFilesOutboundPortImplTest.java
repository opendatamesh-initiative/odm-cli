package org.opendatamesh.cli.usecases.devops.completetask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests of the file reads. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class CompleteTaskFilesOutboundPortImplTest {

    @TempDir
    Path directory;

    private final CompleteTaskFilesOutboundPortImpl port = new CompleteTaskFilesOutboundPortImpl(new ObjectMapper());

    @Test
    void whenLogExistsThenContentIsTrimmed() throws Exception {
        Path log = Files.writeString(directory.resolve("a.log"), "  hello\n");

        ReportFile file = port.readLog(log.toString());

        assertThat(file.readable()).isTrue();
        assertThat(file.content()).isEqualTo("hello");
    }

    @Test
    void whenLogIsMissingThenItIsNotReadable() {
        assertThat(port.readLog(directory.resolve("missing.log").toString()).readable()).isFalse();
        assertThat(port.readLog(directory.toString()).readable()).isFalse();
    }

    @Test
    void whenResultsAreAJsonObjectThenFlagged() throws Exception {
        Path results = Files.writeString(directory.resolve("r.json"), "{\"a\":1}\n");

        ResultsFile file = port.readResults(results.toString());

        assertThat(file.readable()).isTrue();
        assertThat(file.jsonObject()).isTrue();
        assertThat(file.content()).isEqualTo("{\"a\":1}");
    }

    @Test
    void whenResultsAreNotAJsonObjectOrNotJsonThenNotFlagged() throws Exception {
        Path array = Files.writeString(directory.resolve("array.json"), "[1]");
        Path broken = Files.writeString(directory.resolve("broken.json"), "{oops");

        assertThat(port.readResults(array.toString()).jsonObject()).isFalse();
        assertThat(port.readResults(broken.toString()).jsonObject()).isFalse();
        assertThat(port.readResults(broken.toString()).readable()).isTrue();
    }
}

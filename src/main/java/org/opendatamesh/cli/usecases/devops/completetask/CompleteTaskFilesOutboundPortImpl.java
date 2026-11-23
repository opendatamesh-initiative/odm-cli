package org.opendatamesh.cli.usecases.devops.completetask;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads the files as UTF-8 text. The content is the file text, trimmed. */
class CompleteTaskFilesOutboundPortImpl implements CompleteTaskFilesOutboundPort {

    private final ObjectMapper objectMapper;

    CompleteTaskFilesOutboundPortImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportFile readLog(String path) {
        String text = readText(path);
        return new ReportFile(path, text == null ? null : text.trim(), text != null);
    }

    @Override
    public ResultsFile readResults(String path) {
        String text = readText(path);
        if (text == null) {
            return new ResultsFile(path, null, false, false);
        }
        return new ResultsFile(path, text.trim(), true, isJsonObject(text));
    }

    private String readText(String path) {
        Path file = Path.of(path);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            return null;
        }
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isJsonObject(String text) {
        try {
            return objectMapper.readTree(text).isObject();
        } catch (IOException e) {
            return false;
        }
    }
}

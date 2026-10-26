package org.opendatamesh.cli.usecases.config.updater;

import java.util.Map;
import java.util.Set;

// Keys use the dot notation, e.g. cli.cliConfiguration.saveFormat
public record ConfigUpdaterCommand(
        Map<String, String> fieldsToAdd,
        Set<String> fieldsToDelete,
        Map<String, Map<String, String>> arraysEntriesToAdd,
        Map<String, Map<String, String>> arraysEntriesToDelete
) {
}

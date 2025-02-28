package org.opendatamesh.cli.usecases.config.updater;

import java.util.Map;
import java.util.Set;

interface ConfigUpdaterParametersOutboundPort {
    Set<String> getFieldsToDelete();

    Map<String, Map<String, String>> getArraysEntriesToDelete();

    Map<String, String> getFieldsToAdd();

    Map<String, Map<String, String>> getArraysEntriesToAdd();
}

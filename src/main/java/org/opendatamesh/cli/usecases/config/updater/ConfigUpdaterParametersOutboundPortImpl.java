package org.opendatamesh.cli.usecases.config.updater;

import java.util.Map;
import java.util.Set;

class ConfigUpdaterParametersOutboundPortImpl implements ConfigUpdaterParametersOutboundPort {

    private final Map<String, String> simpleEntriesToAdd;
    private final Set<String> keysToDelete;
    private final Map<String, Map<String, String>> arrayAttributesEntriesToAdd;
    private final Map<String, Map<String, String>> arrayAttributesEntriesToDelete;

    public ConfigUpdaterParametersOutboundPortImpl(Map<String, String> simpleEntriesToAdd, Set<String> keysToDelete, Map<String, Map<String, String>> arrayAttributesEntriesToAdd, Map<String, Map<String, String>> arrayAttributesEntriesToDelete) {
        this.simpleEntriesToAdd = simpleEntriesToAdd;
        this.keysToDelete = keysToDelete;
        this.arrayAttributesEntriesToAdd = arrayAttributesEntriesToAdd;
        this.arrayAttributesEntriesToDelete = arrayAttributesEntriesToDelete;
    }

    @Override
    public Set<String> getFieldsToDelete() {
        return keysToDelete;
    }

    @Override
    public Map<String, Map<String, String>> getArraysEntriesToDelete() {
        return arrayAttributesEntriesToDelete;
    }

    @Override
    public Map<String, String> getFieldsToAdd() {
        return simpleEntriesToAdd;
    }

    @Override
    public Map<String, Map<String, String>> getArraysEntriesToAdd() {
        return arrayAttributesEntriesToAdd;
    }
}

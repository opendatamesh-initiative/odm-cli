package org.opendatamesh.cli.usecases.config.updater;

import java.util.Map;
import java.util.Set;

class ConfigUpdaterParametersOutboundPortMock implements ConfigUpdaterParametersOutboundPort {

    private final TestConfigUpdaterInitialState initialState;

    ConfigUpdaterParametersOutboundPortMock(TestConfigUpdaterInitialState initialState) {
        this.initialState = initialState;
    }

    @Override
    public Set<String> getFieldsToDelete() {
        return initialState.getFieldsToDelete();
    }

    @Override
    public Map<String, Map<String, String>> getArraysEntriesToDelete() {
        return initialState.getArraysEntriesToDelete();
    }

    @Override
    public Map<String, String> getFieldsToAdd() {
        return initialState.getFieldsToAdd();
    }

    @Override
    public Map<String, Map<String, String>> getArraysEntriesToAdd() {
        return initialState.getArraysEntriesToAdd();
    }
}

package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TestConfigUpdaterInitialState {
    private Set<String> fieldsToDelete = new HashSet<>();
    private Map<String, String> fieldsToAdd = new HashMap<>();
    private Map<String, Map<String, String>> arraysEntriesToDelete = new HashMap<>();
    private Map<String, Map<String, String>> arraysEntriesToAdd = new HashMap<>();

    private JsonNode defaultConfiguration;

    public Set<String> getFieldsToDelete() {
        return fieldsToDelete;
    }

    public void setFieldsToDelete(Set<String> fieldsToDelete) {
        this.fieldsToDelete = fieldsToDelete;
    }

    public Map<String, String> getFieldsToAdd() {
        return fieldsToAdd;
    }

    public void setFieldsToAdd(Map<String, String> fieldsToAdd) {
        this.fieldsToAdd = fieldsToAdd;
    }

    public Map<String, Map<String, String>> getArraysEntriesToDelete() {
        return arraysEntriesToDelete;
    }

    public void setArraysEntriesToDelete(Map<String, Map<String, String>> arraysEntriesToDelete) {
        this.arraysEntriesToDelete = arraysEntriesToDelete;
    }

    public Map<String, Map<String, String>> getArraysEntriesToAdd() {
        return arraysEntriesToAdd;
    }

    public void setArraysEntriesToAdd(Map<String, Map<String, String>> arraysEntriesToAdd) {
        this.arraysEntriesToAdd = arraysEntriesToAdd;
    }

    public JsonNode getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public void setDefaultConfiguration(JsonNode defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }
}

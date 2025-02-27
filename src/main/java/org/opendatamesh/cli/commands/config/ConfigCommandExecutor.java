package org.opendatamesh.cli.commands.config;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class ConfigCommandExecutor extends PicoCliCommandExecutor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    //E.g. odm-cli config --set cli.cliConfiguration.saveFormat=NORMALIZED cli.cliConfiguration.interactive=true
    private Map<String, String> simpleEntriesToAdd = new HashMap<>();
    //E.g. odm-cli config --unset cli.system
    private Set<String> keysToDelete = new HashSet<>();
    //E.g. odm-cli config --setarray cli.systems name=first nested.field=nestedfieldvalue
    private Map<String, Map<String, String>> arrayAttributesEntriesToAdd = new HashMap<>();
    //E.g. odm-cli config --unsetarray cli.systems name=first
    private Map<String, Map<String, String>> arrayAttributesEntriesToDelete = new HashMap<>();

    private boolean print;

    protected ConfigCommandExecutor(OdmCliConfiguration odmCliConfiguration) {
        super(odmCliConfiguration);
    }

    @Override
    protected void executeUseCase() {
        hanldePrintConfiguration();
        if (nothingToUpdate()) {
            return;
        }
        odmCliConfiguration.updateConfiguration(simpleEntriesToAdd, keysToDelete, arrayAttributesEntriesToAdd, arrayAttributesEntriesToDelete);
    }

    private boolean nothingToUpdate() {
        return CollectionUtils.isEmpty(simpleEntriesToAdd) &&
                CollectionUtils.isEmpty(keysToDelete) &&
                CollectionUtils.isEmpty(arrayAttributesEntriesToAdd) &&
                CollectionUtils.isEmpty(arrayAttributesEntriesToDelete);
    }

    private void hanldePrintConfiguration() {
        if (print) {
            String stringifiedMap = odmCliConfiguration.getAllConfiguration().toString();
            stringifiedMap = stringifiedMap.replace(",", ",\n");
            logger.info("Current Configuration:\n {} \n", stringifiedMap);
        }
    }

    public void setSimpleAttributesToAdd(List<String> rawValues) {
        if (CollectionUtils.isEmpty(rawValues)) {
            return;
        }
        this.simpleEntriesToAdd.putAll(
                parseKeyValuePairs(rawValues)
        );
    }

    public void setAttributesToDelete(List<String> keysToDelete) {
        if (CollectionUtils.isEmpty(keysToDelete)) {
            return;
        }
        this.keysToDelete.addAll(
                parseKeys(keysToDelete)
        );
    }

    public void setArraysEntriesToAdd(List<String> rawValues) {
        if (CollectionUtils.isEmpty(rawValues)) {
            return;
        }
        String arrayField = rawValues.remove(0);
        if (arrayField.contains("=")) {
            throw new IllegalArgumentException("The first command parameter should be the array property name.");
        }
        Map<String, String> arrayValueKeyPairs = parseKeyValuePairs(rawValues);

        this.arrayAttributesEntriesToAdd.put(arrayField, arrayValueKeyPairs);
    }

    public void setArraysEntriesToDelete(List<String> rawValues) {
        if (CollectionUtils.isEmpty(rawValues)) {
            return;
        }
        String arrayField = rawValues.remove(0);
        if (arrayField.contains("=")) {
            throw new IllegalArgumentException("The first command parameter should be the array property name.");
        }
        Map<String, String> arrayValueKeyPairs = parseKeyValuePairs(rawValues);
        this.arrayAttributesEntriesToDelete.put(arrayField, arrayValueKeyPairs);
    }

    //This method parse a list of key value pairs
    //E.g. ["key1=null", "key2=value2", "key3=value3"]
    private Map<String, String> parseKeyValuePairs(List<String> input) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < input.size(); i++) {
            String[] entry = input.get(i).split("=");
            String key = entry[0];
            String value = entry[1];

            if ("null" .equalsIgnoreCase(value)) {
                value = null;
            }
            result.put(key, value);
        }
        return result;
    }

    private Set<String> parseKeys(List<String> input) {
        input.forEach(parameter -> {
            if (parameter.contains("=")) {
                throw new IllegalArgumentException("Parameter " + parameter + " should contain only key, not value.");
            }
        });
        return new HashSet<>(input);
    }

    public void setPrint(boolean print) {
        this.print = print;
    }
}

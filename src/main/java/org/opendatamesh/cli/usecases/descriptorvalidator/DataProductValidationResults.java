package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class DataProductValidationResults {

    private List<Result> results = new ArrayList<>();

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public static class Result {
        private String name;
        private boolean validated;
        private JsonNode validationOutput;

        public Result(String name, boolean validated, JsonNode validationOutput) {
            this.name = name;
            this.validated = validated;
            this.validationOutput = validationOutput;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isValidated() {
            return validated;
        }

        public void setValidated(boolean validated) {
            this.validated = validated;
        }

        public JsonNode getValidationOutput() {
            return validationOutput;
        }

        public void setValidationOutput(JsonNode validationOutput) {
            this.validationOutput = validationOutput;
        }
    }
}

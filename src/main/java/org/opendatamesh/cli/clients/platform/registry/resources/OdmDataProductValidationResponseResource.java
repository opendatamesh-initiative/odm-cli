package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class OdmDataProductValidationResponseResource {

    private OdmDataProductValidationResult syntaxValidationResult;

    private Map<String, OdmDataProductValidationResult> policiesValidationResults = new HashMap<>();

    public OdmDataProductValidationResponseResource() {
    }

    public OdmDataProductValidationResult getSyntaxValidationResult() {
        return syntaxValidationResult;
    }

    public void setSyntaxValidationResult(OdmDataProductValidationResult syntaxValidationResult) {
        this.syntaxValidationResult = syntaxValidationResult;
    }

    public Map<String, OdmDataProductValidationResult> getPoliciesValidationResults() {
        return policiesValidationResults;
    }

    public void setPoliciesValidationResults(Map<String, OdmDataProductValidationResult> policiesValidationResults) {
        this.policiesValidationResults = policiesValidationResults;
    }

    public static class OdmDataProductValidationResult {
        private String name;
        private boolean validated;
        private Object validationOutput;

        public OdmDataProductValidationResult() {
        }

        public OdmDataProductValidationResult(boolean validated, JsonNode validationOutput) {
            this.validated = validated;
            this.validationOutput = validationOutput;
        }

        public boolean isValidated() {
            return validated;
        }

        public void setValidated(boolean validated) {
            this.validated = validated;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValidationOutput() {
            return validationOutput;
        }

        public void setValidationOutput(Object validationOutput) {
            this.validationOutput = validationOutput;
        }
    }
}

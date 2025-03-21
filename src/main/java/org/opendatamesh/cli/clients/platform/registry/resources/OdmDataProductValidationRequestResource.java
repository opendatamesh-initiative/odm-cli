package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class OdmDataProductValidationRequestResource {
    private Boolean validateSyntax = true;
    private Boolean validatePolicies = true;
    private List<String> policyEventTypes = new ArrayList<>();
    private JsonNode dataProductVersion;

    public OdmDataProductValidationRequestResource() {
    }

    public Boolean getValidateSyntax() {
        return validateSyntax;
    }

    public void setValidateSyntax(Boolean validateSyntax) {
        this.validateSyntax = validateSyntax;
    }

    public Boolean getValidatePolicies() {
        return validatePolicies;
    }

    public void setValidatePolicies(Boolean validatePolicies) {
        this.validatePolicies = validatePolicies;
    }

    public List<String> getPolicyEventTypes() {
        return policyEventTypes;
    }

    public void setPolicyEventTypes(List<String> policyEventTypes) {
        this.policyEventTypes = policyEventTypes;
    }

    public JsonNode getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(JsonNode dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

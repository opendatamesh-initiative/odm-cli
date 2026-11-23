package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Every field becomes a query parameter, so only the filters used by the CLI are declared. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryDataProductSearchOptions {
    private String fqn;

    public RegistryDataProductSearchOptions(String fqn) {
        this.fqn = fqn;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }
}

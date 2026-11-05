package org.opendatamesh.cli.clients.platform.registry.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Every field becomes a query parameter, so only the filters used by the CLI are declared. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryDataProductVersionSearchOptions {
    private String dataProductUuid;
    private String versionNumber;

    public RegistryDataProductVersionSearchOptions(String dataProductUuid, String versionNumber) {
        this.dataProductUuid = dataProductUuid;
        this.versionNumber = versionNumber;
    }

    public String getDataProductUuid() {
        return dataProductUuid;
    }

    public void setDataProductUuid(String dataProductUuid) {
        this.dataProductUuid = dataProductUuid;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
}

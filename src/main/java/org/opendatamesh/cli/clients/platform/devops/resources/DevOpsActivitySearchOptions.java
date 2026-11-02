package org.opendatamesh.cli.clients.platform.devops.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Every field becomes a query parameter. {@code statuses} is a comma-separated list. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DevOpsActivitySearchOptions {
    private String dataProductFqn;
    private String dataProductVersionTag;
    private String name;
    private String statuses;

    public DevOpsActivitySearchOptions(String dataProductFqn, String dataProductVersionTag, String name, String statuses) {
        this.dataProductFqn = dataProductFqn;
        this.dataProductVersionTag = dataProductVersionTag;
        this.name = name;
        this.statuses = statuses;
    }

    public String getDataProductFqn() {
        return dataProductFqn;
    }

    public void setDataProductFqn(String dataProductFqn) {
        this.dataProductFqn = dataProductFqn;
    }

    public String getDataProductVersionTag() {
        return dataProductVersionTag;
    }

    public void setDataProductVersionTag(String dataProductVersionTag) {
        this.dataProductVersionTag = dataProductVersionTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatuses() {
        return statuses;
    }

    public void setStatuses(String statuses) {
        this.statuses = statuses;
    }
}

package org.opendatamesh.cli.configs.platform;

public class PlatformConfiguration {
    private PlatformServices services;
    private DevOpsCommandsConfiguration devops = new DevOpsCommandsConfiguration();

    public PlatformServices getServices() {
        return services;
    }

    public void setServices(PlatformServices services) {
        this.services = services;
    }

    public DevOpsCommandsConfiguration getDevops() {
        return devops;
    }

    public void setDevops(DevOpsCommandsConfiguration devops) {
        this.devops = devops;
    }
}

package org.opendatamesh.cli.configs.platform;


public class PlatformServices {
    private PlatformServiceCredentials registry;
    private PlatformServiceCredentials devops;

    public PlatformServiceCredentials getRegistry() {
        return registry;
    }

    public void setRegistry(PlatformServiceCredentials registry) {
        this.registry = registry;
    }

    public PlatformServiceCredentials getDevops() {
        return devops;
    }

    public void setDevops(PlatformServiceCredentials devops) {
        this.devops = devops;
    }
}

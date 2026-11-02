package org.opendatamesh.cli.clients.platform.devops;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.configs.platform.DevOpsCommandsConfiguration;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class PlatformDevOpsClientFactory {

    private final RestTemplate restTemplate;
    private final OdmCliConfiguration configuration;

    public PlatformDevOpsClientFactory(RestTemplate restTemplate, OdmCliConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public PlatformDevOpsClient getClient() {
        if (configuration.getPlatform() == null
                || configuration.getPlatform().getServices() == null
                || configuration.getPlatform().getServices().getDevops() == null
                || !StringUtils.hasText(configuration.getPlatform().getServices().getDevops().getUrl())) {
            throw new IllegalStateException("Missing configuration for odm-platform devops service: cli.platform.services.devops.url");
        }
        PlatformServiceCredentials credentials = configuration.getPlatform().getServices().getDevops();
        DevOpsCommandsConfiguration commands = configuration.getPlatform().getDevops() != null
                ? configuration.getPlatform().getDevops()
                : new DevOpsCommandsConfiguration();
        return new PlatformDevOpsClientImpl(restTemplate, credentials, commands);
    }
}

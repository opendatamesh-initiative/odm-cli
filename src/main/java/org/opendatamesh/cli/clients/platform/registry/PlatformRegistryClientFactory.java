package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class PlatformRegistryClientFactory {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OdmCliConfiguration configuration;

    public PlatformRegistryClient getClient() {
        if (configuration.getPlatform() == null || configuration.getPlatform().getServices() == null || configuration.getPlatform().getServices().getRegistry() == null) {
            throw new IllegalStateException("Missing configuration for odm-platform registry service.");
        }
        if (!StringUtils.hasText(configuration.getPlatform().getServices().getRegistry().getUrl())) {
            throw new IllegalStateException("Missing configuration for odm-platform registry service: cli.platform.services.registry.url");
        }
        return new PlatformRegistryClientImpl(restTemplate, configuration.getPlatform().getServices().getRegistry());
    }
}

package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OdmPlatformRegistryClientFactory {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OdmCliConfiguration configuration;

    public OdmPlatformRegistryClient getClient() {
        if (configuration.getOdmPlatform() == null || configuration.getOdmPlatform().getRegistryService() == null) {
            throw new IllegalStateException("Missing configuration for odm-platform registry service.");
        }
        return new OdmPlatformRegistryClientImpl(restTemplate, configuration.getOdmPlatform().getRegistryService());
    }
}

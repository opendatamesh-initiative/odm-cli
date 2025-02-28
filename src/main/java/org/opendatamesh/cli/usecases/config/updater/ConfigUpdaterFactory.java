package org.opendatamesh.cli.usecases.config.updater;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ConfigUpdaterFactory {
    @Autowired
    private OdmCliConfiguration configuration;
    @Autowired
    private Environment environment;
    @Autowired
    private ResourceLoader resourceLoader;

    public UseCase getConfigUpdater(Map<String, String> simpleEntriesToAdd, Set<String> keysToDelete, Map<String, Map<String, String>> arrayAttributesEntriesToAdd, Map<String, Map<String, String>> arrayAttributesEntriesToDelete) {
        ConfigUpdaterParametersOutboundPort parametersOutboundPort = new ConfigUpdaterParametersOutboundPortImpl(simpleEntriesToAdd, keysToDelete, arrayAttributesEntriesToAdd, arrayAttributesEntriesToDelete);
        ConfigUpdaterPersistenceOutboundPort persistenceOutboundPort = new ConfigUpdaterPersistenceOutboundPortImpl(environment, resourceLoader);
        return new ConfigUpdater(persistenceOutboundPort, parametersOutboundPort);
    }
}

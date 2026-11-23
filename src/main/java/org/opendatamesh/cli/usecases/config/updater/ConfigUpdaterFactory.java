package org.opendatamesh.cli.usecases.config.updater;

import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ConfigUpdaterFactory {
    @Autowired
    private Environment environment;
    @Autowired
    private ResourceLoader resourceLoader;

    public UseCase buildConfigUpdater(ConfigUpdaterCommand command, ConfigUpdaterPresenter presenter) {
        ConfigUpdaterPersistenceOutboundPort persistenceOutboundPort = new ConfigUpdaterPersistenceOutboundPortImpl(environment, resourceLoader);
        return new ConfigUpdater(command, presenter, persistenceOutboundPort);
    }
}

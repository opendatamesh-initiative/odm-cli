package org.opendatamesh.cli.usecases.config.reader;

import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ConfigReaderFactory {

    @Autowired
    private Environment environment;
    @Autowired
    private ResourceLoader resourceLoader;

    public UseCase buildConfigReader(ConfigReaderPresenter presenter) {
        ConfigReaderPersistenceOutboundPort persistenceOutboundPort = new ConfigReaderPersistenceOutboundPortImpl(
                environment,
                resourceLoader
        );
        return new ConfigReader(presenter, persistenceOutboundPort);
    }

}

package org.opendatamesh.cli.usecases.config.reader;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.UseCaseReturning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfigReaderFactory {

    @Autowired
    private OdmCliConfiguration configuration;
    @Autowired
    private Environment environment;
    @Autowired
    private ResourceLoader resourceLoader;

    public UseCaseReturning<Map<String, String>> getConfigReader() {
        ConfigReaderPersistenceOutboundPort persistenceOutboundPort = new ConfigReaderPersistenceOutboundPortImpl(
                environment,
                resourceLoader
        );
        return new ConfigReader(persistenceOutboundPort);
    }

}

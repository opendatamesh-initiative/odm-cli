package org.opendatamesh.cli.configs;

import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OdmCliConfiguration.class)
public class OdmCliBaseConfigurationLoader {

    private final OdmCliConfiguration odmCliConfiguration;

    public OdmCliBaseConfigurationLoader(OdmCliConfiguration odmCliConfiguration) {
        this.odmCliConfiguration = odmCliConfiguration;
    }

    @Bean
    public OdmCliBaseConfiguration odmCliBaseConfiguration() {
        OdmCliBaseConfiguration baseConfiguration = new OdmCliBaseConfiguration();
        baseConfiguration.setCliConfiguration(odmCliConfiguration.getCliConfiguration());
        baseConfiguration.setRemoteSystemsConfigurations(odmCliConfiguration.getRemoteSystemsConfigurations());
        baseConfiguration.setEnv(odmCliConfiguration.getEnvAsMap());
        return baseConfiguration;
    }
}

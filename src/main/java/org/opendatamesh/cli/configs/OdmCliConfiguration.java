package org.opendatamesh.cli.configs;

import org.opendatamesh.cli.extensions.OdmCliBaseConfiguration;
import org.opendatamesh.cli.usecases.config.reader.ConfigReaderFactory;
import org.opendatamesh.cli.usecases.config.updater.ConfigUpdaterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This beans has "thread" scope. This allows its state to be accessed and modified
 * by other beans. This behaviour is used to overwrite default configuration values
 * by cli commands options and to allow a shared configuration context among all
 * Spring components.
 */
@Component
@ConfigurationProperties(prefix = "cli")
@Scope("thread")
public class OdmCliConfiguration {

    private Config cliConfiguration;
    private List<OdmCliBaseConfiguration.SystemConfig> systems;
    private OdmPlatform odmPlatform;

    @Autowired
    @Lazy
    private ConfigReaderFactory readerFactory;

    @Autowired
    @Lazy
    private ConfigUpdaterFactory updaterFactory;

    public Map<String, String> getAllConfiguration() {
        return readerFactory.getConfigReader().execute();
    }

    public void updateConfiguration(Map<String, String> simpleEntriesToAdd, Set<String> keysToDelete, Map<String, Map<String, String>> arrayAttributesEntriesToAdd, Map<String, Map<String, String>> arrayAttributesEntriesToDelete) {
        updaterFactory.getConfigUpdater(simpleEntriesToAdd, keysToDelete, arrayAttributesEntriesToAdd, arrayAttributesEntriesToDelete).execute();
    }

    public OdmCliBaseConfiguration getBaseConfiguration() {
        OdmCliBaseConfiguration baseConfiguration = new OdmCliBaseConfiguration();
        baseConfiguration.setCliConfiguration(getCliConfiguration());
        baseConfiguration.setSystems(getSystems());
        return baseConfiguration;
    }

    public Config getCliConfiguration() {
        return cliConfiguration;
    }

    public void setCliConfiguration(Config cliConfiguration) {
        this.cliConfiguration = cliConfiguration;
    }

    public List<OdmCliBaseConfiguration.SystemConfig> getSystems() {
        return systems;
    }

    public void setSystems(List<OdmCliBaseConfiguration.SystemConfig> systems) {
        this.systems = systems;
    }

    public OdmPlatform getOdmPlatform() {
        return odmPlatform;
    }

    public void setOdmPlatform(OdmPlatform odmPlatform) {
        this.odmPlatform = odmPlatform;
    }

    public static class Config extends OdmCliBaseConfiguration.Config {
        private String saveFormat;
        private Boolean interactive;

        public String getSaveFormat() {
            return Optional.ofNullable(saveFormat)
                    .map(String::toUpperCase)
                    .orElse(null);
        }

        public void setSaveFormat(String saveFormat) {
            this.saveFormat = saveFormat;
        }

        public Boolean isInteractive() {
            return interactive;
        }

        public void setInteractive(Boolean interactive) {
            this.interactive = interactive;
        }
    }

    public static class OdmPlatform {
        private OdmPlatformService registryService;

        public OdmPlatformService getRegistryService() {
            return registryService;
        }

        public void setRegistryService(OdmPlatformService registryService) {
            this.registryService = registryService;
        }
    }

    public static class OdmPlatformService {
        private String endpoint;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }
}

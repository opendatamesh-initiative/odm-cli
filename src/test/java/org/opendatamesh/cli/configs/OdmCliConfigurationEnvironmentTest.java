package org.opendatamesh.cli.configs;

import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Configuration read from environment variables, with the configuration file as the fallback, as Spring Boot orders its
 * sources: the environment variables come before the external configuration file.
 * Generated from BDMD-5443, PR #16 review ("le puoi prendere o da env o da config file, per docker andremo di default sulle env").
 */
class OdmCliConfigurationEnvironmentTest {

    private static OdmCliConfiguration bind(Map<String, Object> environment, Map<String, Object> configFile) {
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, environment));
        sources.addLast(new MapPropertySource("configFile", configFile));
        return new Binder(ConfigurationPropertySources.from(sources)).bindOrCreate("cli", OdmCliConfiguration.class);
    }

    /**
     * Scenario: An environment variable wins over the configuration file.
     * Given the DevOps URL both in an environment variable and in the configuration file
     * When the configuration is bound
     * Then the URL of the environment variable is used
     */
    @Test
    void whenUrlIsInEnvironmentAndFileThenEnvironmentWins() {
        OdmCliConfiguration configuration = bind(
                Map.of("CLI_PLATFORM_SERVICES_DEVOPS_URL", "https://env.example.com/devops"),
                Map.of("cli.platform.services.devops.url", "https://file.example.com/devops"));

        assertThat(configuration.getPlatform().getServices().getDevops().getUrl()).isEqualTo("https://env.example.com/devops");
    }

    /**
     * Scenario: The configuration file is the fallback.
     * Given the Registry URL only in the configuration file
     * When the configuration is bound
     * Then the URL of the file is used
     */
    @Test
    void whenUrlIsOnlyInFileThenFileIsUsed() {
        OdmCliConfiguration configuration = bind(
                Map.of(),
                Map.of("cli.platform.services.registry.url", "https://file.example.com/registry"));

        assertThat(configuration.getPlatform().getServices().getRegistry().getUrl()).isEqualTo("https://file.example.com/registry");
    }

    /**
     * Scenario: camelCase, nested and list keys bind from environment variables.
     * Given the DevOps tuning, OAuth2 credentials and a header set only as environment variables
     * When the configuration is bound
     * Then each value reaches its key
     */
    @Test
    void whenNestedAndListKeysAreInEnvironmentThenTheyBind() {
        OdmCliConfiguration configuration = bind(
                Map.of(
                        "CLI_PLATFORM_DEVOPS_POLLINTERVAL", "10s",
                        "CLI_PLATFORM_SERVICES_DEVOPS_OAUTH2_CLIENTID", "pipeline-client",
                        "CLI_PLATFORM_SERVICES_DEVOPS_OAUTH2_CLIENTSECRET", "secret",
                        "CLI_PLATFORM_SERVICES_DEVOPS_HEADERS_0_NAME", "X-Tenant",
                        "CLI_PLATFORM_SERVICES_DEVOPS_HEADERS_0_VALUE", "sales",
                        "CLI_CLICONFIGURATION_INTERACTIVE", "false"),
                Map.of());

        PlatformServiceCredentials devops = configuration.getPlatform().getServices().getDevops();
        assertThat(configuration.getPlatform().getDevops().getPollInterval()).isEqualTo(Duration.ofSeconds(10));
        assertThat(devops.getOauth2().getClientId()).isEqualTo("pipeline-client");
        assertThat(devops.getOauth2().getClientSecret()).isEqualTo("secret");
        assertThat(devops.getHeaders()).singleElement().satisfies(header -> {
            assertThat(header.getName()).isEqualTo("X-Tenant");
            assertThat(header.getValue()).isEqualTo("sales");
        });
        assertThat(configuration.getCliConfiguration().isInteractive()).isFalse();
    }
}

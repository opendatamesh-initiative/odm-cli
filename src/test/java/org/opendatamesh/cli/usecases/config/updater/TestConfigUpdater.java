package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.opendatamesh.cli.usecases.importer.referencehandler.utils.JacksonUtils.parserFixModule;

public class TestConfigUpdater {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(parserFixModule());


    @Test
    public void testConfigUpdater() throws IOException {

        TestConfigUpdaterInitialState initialState = objectMapper.readValue(
                Resources.toByteArray(
                        getClass().getResource("test_config_updater_initial_state.json")
                ),
                TestConfigUpdaterInitialState.class
        );
        TestConfigUpdaterFinalState finalState = new TestConfigUpdaterFinalState();
        ConfigUpdaterParametersOutboundPort parametersOutboundPort = new ConfigUpdaterParametersOutboundPortMock(initialState);
        ConfigUpdaterPersistenceOutboundPort persistenceOutboundPort = new ConfigUpdaterPersistenceOutboundPortMock(initialState, finalState);

        new ConfigUpdater(persistenceOutboundPort, parametersOutboundPort).execute();

        TestConfigUpdaterFinalState expectedFinalState = objectMapper.readValue(
                getClass().getResource("test_config_updater_final_state.json"),
                TestConfigUpdaterFinalState.class
        );
        Assertions.assertThat(finalState)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedFinalState);
    }
}

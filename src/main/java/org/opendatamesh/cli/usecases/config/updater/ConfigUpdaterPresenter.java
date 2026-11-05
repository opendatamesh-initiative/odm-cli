package org.opendatamesh.cli.usecases.config.updater;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.cli.utils.usecases.UseCasePresenter;

public interface ConfigUpdaterPresenter extends UseCasePresenter {
    void presentConfigurationUpdated(JsonNode savedConfiguration);
}

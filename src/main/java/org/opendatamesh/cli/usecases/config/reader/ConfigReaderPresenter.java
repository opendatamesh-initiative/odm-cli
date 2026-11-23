package org.opendatamesh.cli.usecases.config.reader;

import org.opendatamesh.cli.utils.usecases.UseCasePresenter;

import java.util.Map;

public interface ConfigReaderPresenter extends UseCasePresenter {
    void presentConfiguration(Map<String, String> configuration);
}

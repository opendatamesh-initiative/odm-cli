package org.opendatamesh.cli.usecases.init;

import org.opendatamesh.cli.utils.usecases.UseCasePresenter;
import org.opendatamesh.dpds.model.DataProductVersion;

public interface DataProductDescriptorInitializerPresenter extends UseCasePresenter {
    void presentDescriptorInitialized(DataProductVersion descriptor);
}

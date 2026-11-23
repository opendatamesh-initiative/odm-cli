package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.utils.usecases.UseCasePresenter;
import org.opendatamesh.dpds.model.DataProductVersion;

public interface ImporterPresenter extends UseCasePresenter {
    void presentDescriptorImported(DataProductVersion descriptor);
}

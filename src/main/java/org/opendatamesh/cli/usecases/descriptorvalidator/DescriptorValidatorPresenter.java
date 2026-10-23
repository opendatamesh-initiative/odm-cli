package org.opendatamesh.cli.usecases.descriptorvalidator;

import org.opendatamesh.cli.utils.usecases.UseCasePresenter;

public interface DescriptorValidatorPresenter extends UseCasePresenter {
    void presentValidationResults(DataProductValidationResults validationResults);
}

package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.utils.usecases.UseCasePresenter;

public interface CompleteTaskPresenter extends UseCasePresenter {
    void presentProgress(CompleteTaskStep step, String detail);

    void presentFailedStep(CompleteTaskStep step);

    void presentTaskCompleted(String providerRunId, ExecutionStatus recordedStatus);

    void presentResultsUploaded(String providerRunId);
}

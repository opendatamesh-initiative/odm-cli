package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.utils.usecases.UseCasePresenter;

public interface StartTaskPresenter extends UseCasePresenter {
    void presentProgress(StartTaskStep step, String detail);

    /** One call per poll while waiting for an approval. Shown at DEBUG level by the command. */
    default void presentPoll(StartTaskStep step, String detail) {
    }

    void presentWarning(String message);

    void presentFailedStep(StartTaskStep step);

    void presentTaskStarted(DevOpsActivity activity, DevOpsTask task);
}

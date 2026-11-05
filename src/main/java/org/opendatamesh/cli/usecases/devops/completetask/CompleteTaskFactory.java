package org.opendatamesh.cli.usecases.devops.completetask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.cli.clients.platform.devops.PlatformDevOpsClientFactory;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.stereotype.Component;

@Component
public class CompleteTaskFactory {

    private final PlatformDevOpsClientFactory devOpsClientFactory;
    private final ObjectMapper objectMapper;

    public CompleteTaskFactory(PlatformDevOpsClientFactory devOpsClientFactory, ObjectMapper objectMapper) {
        this.devOpsClientFactory = devOpsClientFactory;
        this.objectMapper = objectMapper;
    }

    public UseCase buildCompleteTask(CompleteTaskCommand command, CompleteTaskPresenter presenter) {
        return new CompleteTask(
                command,
                presenter,
                new CompleteTaskDevOpsOutboundPortImpl(devOpsClientFactory.getClient()),
                new CompleteTaskFilesOutboundPortImpl(objectMapper),
                new CompleteTaskClockOutboundPortImpl()
        );
    }
}

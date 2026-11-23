package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.PlatformDevOpsClientFactory;
import org.opendatamesh.cli.clients.platform.registry.PlatformRegistryClientFactory;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.springframework.stereotype.Component;

@Component
public class StartTaskFactory {

    private final PlatformDevOpsClientFactory devOpsClientFactory;
    private final PlatformRegistryClientFactory registryClientFactory;

    public StartTaskFactory(PlatformDevOpsClientFactory devOpsClientFactory, PlatformRegistryClientFactory registryClientFactory) {
        this.devOpsClientFactory = devOpsClientFactory;
        this.registryClientFactory = registryClientFactory;
    }

    public UseCase buildStartTask(StartTaskCommand command, StartTaskPresenter presenter) {
        return new StartTask(
                command,
                presenter,
                new StartTaskDevOpsOutboundPortImpl(devOpsClientFactory.getClient()),
                new StartTaskRegistryOutboundPortImpl(registryClientFactory::getClient),
                new StartTaskDescriptorOutboundPortImpl(),
                new StartTaskClockOutboundPortImpl()
        );
    }
}

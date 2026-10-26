package org.opendatamesh.cli.usecases.init;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.model.core.ExternalDocs;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.info.Owner;
import org.opendatamesh.dpds.model.interfaces.*;
import org.opendatamesh.dpds.model.internals.InternalComponents;
import java.util.ArrayList;

class DataProductDescriptorInitializer implements UseCase {

    private final DataProductDescriptorInitializerCommand command;
    private final DataProductDescriptorInitializerPresenter presenter;
    private final DataProductDescriptorInitializerParserOutboundPort parserOutboundPort;

    DataProductDescriptorInitializer(DataProductDescriptorInitializerCommand command, DataProductDescriptorInitializerPresenter presenter, DataProductDescriptorInitializerParserOutboundPort parserOutboundPort) {
        this.command = command;
        this.presenter = presenter;
        this.parserOutboundPort = parserOutboundPort;
    }

    @Override
    public void execute() {
        try {
            DataProductVersion defaultDescriptor = createDefaultDataProductVersionFile();
            parserOutboundPort.saveObject(defaultDescriptor, command.outputFile(), command.force());
            presenter.presentDescriptorInitialized(defaultDescriptor);
        } catch (Exception e) {
            presenter.presentException(e);
        }
    }

    private DataProductVersion createDefaultDataProductVersionFile() {
        DataProductVersion descriptor = new DataProductVersion();
        descriptor.setInfo(createDefaultInfo());
        descriptor.setInterfaceComponents(new InterfaceComponents());
        descriptor.setInternalComponents(new InternalComponents());
        descriptor.setComponents(new Components());
        descriptor.setTags(new ArrayList<>());
        descriptor.setExternalDocs(new ExternalDocs());
        return descriptor;
    }

    private Info createDefaultInfo() {

        Info descriptorInfo = new Info();

        descriptorInfo.setDomain(command.domain());
        descriptorInfo.setName(command.name());
        descriptorInfo.setVersion(command.descriptorVersion());
        descriptorInfo.setDescription(command.description());

        Owner infoOwner = new Owner();
        infoOwner.setId(command.ownerId() != null ? command.ownerId() : "{defaultOwnerId}");
        infoOwner.setName(command.ownerName() != null ? command.ownerName() : "{defaultOwnerName}");
        descriptorInfo.setOwner(infoOwner);

        if (command.displayName() != null) {
            descriptorInfo.setDisplayName(command.displayName());
        } else {
            descriptorInfo.setDisplayName(command.name());
        }

        if (command.fullyQualifiedName() != null) {
            descriptorInfo.setFullyQualifiedName(command.fullyQualifiedName());
        } else {
            //The last segment has always been "null": the CLI never provided the "version" option it was read from
            descriptorInfo.setFullyQualifiedName(String.format("%s:%s:%s",
                    command.domain(), command.name(), null));
        }

        return descriptorInfo;

    }
}

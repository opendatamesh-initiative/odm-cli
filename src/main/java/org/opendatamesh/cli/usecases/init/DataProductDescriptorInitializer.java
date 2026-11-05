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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DataProductDescriptorInitializer implements UseCase {

    private static final Pattern MAJOR_VERSION_PATTERN = Pattern.compile("^(\\d+)");

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
            descriptorInfo.setFullyQualifiedName(buildDefaultFullyQualifiedName());
        }

        return descriptorInfo;

    }

    //DPDS convention: urn:dpds:{domain}:dataproducts:{name}:{major version}, the version segment only when known
    private String buildDefaultFullyQualifiedName() {
        String fullyQualifiedName = String.format("urn:dpds:%s:dataproducts:%s", command.domain(), command.name());
        return extractMajorVersion(command.descriptorVersion())
                .map(majorVersion -> fullyQualifiedName + ":" + majorVersion)
                .orElse(fullyQualifiedName);
    }

    private Optional<String> extractMajorVersion(String version) {
        if (version == null) {
            return Optional.empty();
        }
        Matcher matcher = MAJOR_VERSION_PATTERN.matcher(version.trim());
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}

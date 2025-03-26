package org.opendatamesh.cli.usecases.init;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.model.core.ExternalDocs;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.info.Owner;
import org.opendatamesh.dpds.model.interfaces.*;
import org.opendatamesh.dpds.model.internals.InternalComponents;
import java.util.*;

class Init implements UseCase {

    private final InitParameterOutboundPort parameterOutboundPort;
    private final InitParserOutboundPort parserOutboundPort;

    Init(InitParameterOutboundPort parameterOutboundPort, InitParserOutboundPort parserOutboundPort) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
    }

    @Override
    public void execute() {
        ImporterArguments arguments = parameterOutboundPort.getImporterArguments();
        createDefaultDataProductVersionFile(arguments.getParentCommandOptions());
    }

    private void createDefaultDataProductVersionFile(Map<String, String> args) {
        DataProductVersion descriptor = new DataProductVersion();
        descriptor.setInfo(createDefaultInfo(args));
        descriptor.setInterfaceComponents(new InterfaceComponents());
        descriptor.setInternalComponents(new InternalComponents());
        descriptor.setComponents(new Components());
        descriptor.setTags(new ArrayList<>());
        descriptor.setExternalDocs(new ExternalDocs());
        parserOutboundPort.saveObject(descriptor, args.get("outputFile"), args.containsKey("force"));
    }

    private Info createDefaultInfo(Map<String, String> args) {

        Info descriptorInfo = new Info();

        descriptorInfo.setDomain(args.get("domain"));
        descriptorInfo.setName(args.get("name"));
        descriptorInfo.setVersion(args.get("descriptorVersion"));
        descriptorInfo.setDescription(args.get("description"));

        Owner infoOwner = new Owner();
        infoOwner.setId(args.getOrDefault("ownerId", "{defaultOwnerId}"));
        infoOwner.setName(args.getOrDefault("ownerName", "{defaultOwnerName}"));
        descriptorInfo.setOwner(infoOwner);

        if (args.containsKey("displayName")) {
            descriptorInfo.setDisplayName(args.get("displayName"));
        } else {
            descriptorInfo.setDisplayName(args.get("name"));
        }

        if (args.containsKey("fullyQualifiedName")) {
            descriptorInfo.setFullyQualifiedName(args.get("fullyQualifiedName"));
        } else {
            descriptorInfo.setFullyQualifiedName(String.format("%s:%s:%s",
                    args.get("domain"), args.get("name"), args.get("version")));
        }

        return descriptorInfo;

    }
}

package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.cli.utils.usecases.UseCaseException;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.interfaces.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PortImporter implements UseCase {

    private static final Logger log = LoggerFactory.getLogger(PortImporter.class);
    private final ImporterCommand command;
    private final ImporterPresenter presenter;
    private final ImporterConfigurationOutboundPort configurationOutboundPort;
    private final ImporterParserOutboundPort parserOutboundPort;
    private final ImporterExtension<Port> importerExtension;

    PortImporter(ImporterCommand command, ImporterPresenter presenter, ImporterConfigurationOutboundPort configurationOutboundPort, ImporterParserOutboundPort parserOutboundPort, ImporterExtension<Port> importerExtension) {
        this.command = command;
        this.presenter = presenter;
        this.configurationOutboundPort = configurationOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importerExtension = importerExtension;
    }

    @Override
    public void execute() {
        try {
            checkDescriptorFile();
            DataProductVersion descriptor = readAndParseDataProductVersion();
            ImporterArguments arguments = buildImporterArguments(descriptor);
            Optional<Port> existingPort = findPortIfExists(descriptor);
            logInformationAboutExistingPort(existingPort);
            Port port = importerExtension.importElement(existingPort.orElse(null), arguments);
            addPortToDescriptor(descriptor, port);
            parserOutboundPort.saveDescriptor(descriptor);
            presenter.presentDescriptorImported(descriptor);
        } catch (Exception e) {
            presenter.presentException(e);
        }
    }

    private void checkDescriptorFile() {
        String descriptorFilePath = command.descriptorFilePath();
        if (descriptorFilePath == null || descriptorFilePath.trim().isEmpty()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Descriptor root path must not be empty.");
        }
        if (!parserOutboundPort.descriptorExists()) {
            throw new UseCaseException(UseCaseException.Reason.NOT_FOUND, "Descriptor root path does not exist: " + descriptorFilePath);
        }
        if (parserOutboundPort.isDescriptorDirectory()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Descriptor root path must be a file, not a directory: " + descriptorFilePath);
        }
        if (parserOutboundPort.isDescriptorEmpty()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Descriptor file is empty: " + descriptorFilePath);
        }
    }

    private ImporterArguments buildImporterArguments(DataProductVersion descriptor) {
        ImporterArguments arguments = new ImporterArguments();
        arguments.setRootDescriptorPath(Path.of(command.descriptorFilePath()));
        arguments.setOdmCliConfig(configurationOutboundPort.getOdmCliConfig());
        arguments.setParentCommandOptions(command.parentCommandOptions());
        arguments.setDataProductVersion(descriptor);
        return arguments;
    }

    private DataProductVersion readAndParseDataProductVersion() {
        try {
            return parserOutboundPort.getDataProductVersion();
        } catch (FileNotFoundException e) {
            throw new UseCaseException(UseCaseException.Reason.NOT_FOUND, e.getMessage(), e);
        }
    }

    private void logInformationAboutExistingPort(Optional<Port> existingPort) {
        existingPort.ifPresentOrElse(
                port -> log.info("Existing port found with name: {} and fqn: {}",
                        port.getName(),
                        port.getFullyQualifiedName()),
                () -> log.info("No existing port found with type: {} and name: {}. Creating a new port.",
                        command.to(),
                        command.target())
        );
    }

    private Optional<Port> findPortIfExists(DataProductVersion descriptor) {
        return getPortByTypeAndName(descriptor, command.to(), command.target());
    }

    private void addPortToDescriptor(DataProductVersion descriptor, Port port) {
        String toOption = getToOption();
        fixPortFqn(descriptor, port, toOption);
        Map<String, Consumer<DataProductVersion>> handlers = getPortAdditionToTargetHandler(port);

        Consumer<DataProductVersion> handler = handlers.getOrDefault(
                toOption,
                d -> {
                    throw new UseCaseException(UseCaseException.Reason.INVALID, "Invalid to option for port importer command: " + toOption);
                }
        );

        handler.accept(descriptor);
    }

    private String getToOption() {
        String toOption = command.to();
        if (!StringUtils.hasText(toOption)) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "'to' option for import command is not specified.");
        }
        return toOption;
    }

    private void fixPortFqn(DataProductVersion descriptor, Port port, String toOption) {
        if (!StringUtils.hasText(port.getFullyQualifiedName())) {
            String collectionName = toOption.replace("-", "") + "s";
            String fqn = String.format(
                    "%s:%s:%s:%s",
                    descriptor.getInfo().getFullyQualifiedName(),
                    collectionName,
                    port.getName(),
                    extractMajorVersion(port.getVersion())
            );
            port.setFullyQualifiedName(fqn);
        }
    }

    private Map<String, Function<DataProductVersion, List<Port>>> getPortRetrievers() {
        Map<String, Function<DataProductVersion, List<Port>>> portRetrievers = new HashMap<>();

        portRetrievers.put("input-port", d -> d.getInterfaceComponents().getInputPorts());
        portRetrievers.put("output-port", d -> d.getInterfaceComponents().getOutputPorts());
        portRetrievers.put("discovery-port", d -> d.getInterfaceComponents().getDiscoveryPorts());
        portRetrievers.put("observability-port", d -> d.getInterfaceComponents().getObservabilityPorts());
        portRetrievers.put("control-port", d -> d.getInterfaceComponents().getControlPorts());

        return portRetrievers;
    }

    private Optional<Port> getPortByTypeAndName(DataProductVersion dataProduct, String portType, String portName) {
        return Optional.ofNullable(getPortRetrievers().get(portType))
                .map(retriever -> retriever.apply(dataProduct))
                .flatMap(ports -> ports.stream().filter(p -> p.getName().equals(portName)).findFirst());
    }

    private Map<String, Consumer<DataProductVersion>> getPortAdditionToTargetHandler(Port port) {
        Map<String, Consumer<DataProductVersion>> actions = new HashMap<>();
        actions.put("input-port", d -> d.getInterfaceComponents().setInputPorts(
                replacePort(d.getInterfaceComponents().getInputPorts(), port)));
        actions.put("output-port", d -> d.getInterfaceComponents().setOutputPorts(
                replacePort(d.getInterfaceComponents().getOutputPorts(), port)));
        actions.put("discovery-port", d -> d.getInterfaceComponents().setDiscoveryPorts(
                replacePort(d.getInterfaceComponents().getDiscoveryPorts(), port)));
        actions.put("observability-port", d -> d.getInterfaceComponents().setObservabilityPorts(
                replacePort(d.getInterfaceComponents().getObservabilityPorts(), port)));
        actions.put("control-port", d -> d.getInterfaceComponents().setControlPorts(
                replacePort(d.getInterfaceComponents().getControlPorts(), port)));
        return actions;
    }

    private List<Port> replacePort(List<Port> ports, Port port) {
        List<Port> result = new ArrayList<>(ports != null ? ports : Collections.emptyList());
        result.removeIf(p -> port.getFullyQualifiedName().equals(p.getFullyQualifiedName()));
        result.add(port);
        return result;
    }

    private String extractMajorVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Version string cannot be null or empty.");
        }
        Pattern pattern = Pattern.compile("^(\\d+)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new UseCaseException(UseCaseException.Reason.INVALID, "Invalid version format: " + version);
        }
    }

}

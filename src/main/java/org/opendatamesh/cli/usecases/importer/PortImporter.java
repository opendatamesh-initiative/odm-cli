package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PortImporter implements UseCase {

    private static final Logger log = LoggerFactory.getLogger(PortImporter.class);
    private final PortImporterParameterOutboundPort parameterOutboundPort;
    private final PortImporterParserOutboundPort parserOutboundPort;
    private final ImporterExtension<PortDPDS> importerExtension;

    PortImporter(PortImporterParameterOutboundPort parameterOutboundPort, PortImporterParserOutboundPort parserOutboundPort, ImporterExtension<PortDPDS> importerExtension) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importerExtension = importerExtension;
    }

    @Override
    public void execute() {
        DataProductVersionDPDS descriptor = parserOutboundPort.getDataProductVersion();
        ImporterArguments arguments = parameterOutboundPort.getImporterArguments();
        arguments.setDataProductVersion(descriptor);
        Optional<PortDPDS> existingPort = findPortIfExists(descriptor, arguments);
        logInformationAboutExistingPort(existingPort, arguments);
        PortDPDS port = importerExtension.importElement(existingPort.orElse(null), arguments);
        addPortToDescriptor(descriptor, port);
        parserOutboundPort.saveDescriptor(descriptor);
    }

    private void logInformationAboutExistingPort(Optional<PortDPDS> existingPort, ImporterArguments arguments) {
        existingPort.ifPresentOrElse(
                port -> log.info("Existing port found with name: {} and fqn: {}",
                        port.getName(),
                        port.getFullyQualifiedName()),
                () -> log.info("No existing port found with type: {} and name: {}. Creating a new port.",
                        arguments.getParentCommandOptions().get("to"),
                        arguments.getParentCommandOptions().get("target"))
        );
    }

    private Optional<PortDPDS> findPortIfExists(DataProductVersionDPDS descriptor, ImporterArguments arguments) {
        String portType = arguments.getParentCommandOptions().get("to");
        String portName = arguments.getParentCommandOptions().get("target");
        return getPortByTypeAndName(descriptor, portType, portName);
    }

    private void addPortToDescriptor(DataProductVersionDPDS descriptor, PortDPDS port) {
        String toOption = getToOption();
        fixPortFqn(descriptor, port, toOption);
        Map<String, Consumer<DataProductVersionDPDS>> handlers = getPortAdditionToTargetHandler(port);

        Consumer<DataProductVersionDPDS> handler = handlers.getOrDefault(
                toOption,
                d -> {
                    throw new IllegalArgumentException("Invalid to option for port importer command: " + toOption);
                }
        );

        handler.accept(descriptor);
    }

    private String getToOption() {
        String toOption = parameterOutboundPort.getImporterArguments().getParentCommandOptions().get("to");
        if (!StringUtils.hasText(toOption)) {
            throw new IllegalArgumentException("'to' option for import command is not specified.");
        }
        return toOption;
    }

    private void fixPortFqn(DataProductVersionDPDS descriptor, PortDPDS port, String toOption) {
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

    private Map<String, Function<DataProductVersionDPDS, List<PortDPDS>>> getPortRetrievers() {
        Map<String, Function<DataProductVersionDPDS, List<PortDPDS>>> portRetrievers = new HashMap<>();

        portRetrievers.put("input-port", d -> d.getInterfaceComponents().getInputPorts());
        portRetrievers.put("output-port", d -> d.getInterfaceComponents().getOutputPorts());
        portRetrievers.put("discovery-port", d -> d.getInterfaceComponents().getDiscoveryPorts());
        portRetrievers.put("observability-port", d -> d.getInterfaceComponents().getObservabilityPorts());
        portRetrievers.put("control-port", d -> d.getInterfaceComponents().getControlPorts());

        return portRetrievers;
    }

    private Optional<PortDPDS> getPortByTypeAndName(DataProductVersionDPDS dataProduct, String portType, String portName) {
        return Optional.ofNullable(getPortRetrievers().get(portType))
                .map(retriever -> retriever.apply(dataProduct))
                .flatMap(ports -> ports.stream().filter(p -> p.getName().equals(portName)).findFirst());
    }

    private Map<String, Consumer<DataProductVersionDPDS>> getPortAdditionToTargetHandler(PortDPDS port) {
        Map<String, Consumer<DataProductVersionDPDS>> actions = new HashMap<>();
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

    private List<PortDPDS> replacePort(List<PortDPDS> ports, PortDPDS port) {
        List<PortDPDS> result = new ArrayList<>(ports != null ? ports : Collections.emptyList());
        result.removeIf(p -> port.getFullyQualifiedName().equals(p.getFullyQualifiedName()));
        result.add(port);
        return result;
    }

    private String extractMajorVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty.");
        }
        Pattern pattern = Pattern.compile("^(\\d+)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }
    }

}

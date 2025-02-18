package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PortImporter implements UseCase {

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
        PortDPDS port = importerExtension.importElement(null, arguments);
        addPortToDescriptor(descriptor, port);
        parserOutboundPort.saveDescriptor(descriptor);
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

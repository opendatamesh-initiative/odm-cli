package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaArguments;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.usecases.UseCase;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ImportSchema implements UseCase {

    private final ImportSchemaParameterOutboundPort parameterOutboundPort;
    private final ImportSchemaParserOutboundPort parserOutboundPort;
    private final ImportSchemaExtension importSchemaExtension;

    ImportSchema(ImportSchemaParameterOutboundPort parameterOutboundPort, ImportSchemaParserOutboundPort parserOutboundPort, ImportSchemaExtension importSchemaExtension) {
        this.parameterOutboundPort = parameterOutboundPort;
        this.parserOutboundPort = parserOutboundPort;
        this.importSchemaExtension = importSchemaExtension;
    }

    @Override
    public void execute() {
        DataProductVersionDPDS descriptor = parserOutboundPort.getDataProductVersion();
        ImportSchemaArguments arguments = parameterOutboundPort.getImportSchemaArguments();
        PortDPDS port = importSchemaExtension.importElement(arguments);
        addPortToDescriptor(descriptor, port);
        parserOutboundPort.saveDescriptor(descriptor);
    }

    private void addPortToDescriptor(DataProductVersionDPDS descriptor, PortDPDS port) {
        String target = getTargetOption();
        fixPortFqn(descriptor, port, target);
        Map<String, Consumer<DataProductVersionDPDS>> handlers = getPortAdditionToTargetHandler(port);

        Consumer<DataProductVersionDPDS> handler = handlers.getOrDefault(
                target.toLowerCase(),
                d -> {
                    throw new IllegalArgumentException("Invalid target option for import command: " + target);
                }
        );

        handler.accept(descriptor);
    }

    private String getTargetOption() {
        String target = parameterOutboundPort.getImportSchemaArguments().getParentCommandOptions().get("target");
        if (!StringUtils.hasText(target)) {
            throw new IllegalArgumentException("Target option for import command is not specified.");
        }
        return target;
    }

    private void fixPortFqn(DataProductVersionDPDS descriptor, PortDPDS port, String target) {
        if (!StringUtils.hasText(port.getFullyQualifiedName())) {
            String collectionName = target.replace("-", "") + "s";
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

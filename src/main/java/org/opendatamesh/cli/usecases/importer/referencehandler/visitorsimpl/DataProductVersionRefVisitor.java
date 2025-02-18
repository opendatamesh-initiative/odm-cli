package org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl;

import org.opendatamesh.cli.usecases.importer.referencehandler.ReferenceHandler;
import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.core.ComponentRefVisitor;
import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.interfaces.InterfaceComponentsRefVisitor;
import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.internals.InternalComponentsDPDSRefVisitor;
import org.opendatamesh.dpds.model.core.ComponentsDPDS;
import org.opendatamesh.dpds.model.core.ExternalResourceDPDS;
import org.opendatamesh.dpds.model.info.InfoDPDS;
import org.opendatamesh.dpds.model.interfaces.InterfaceComponentsDPDS;
import org.opendatamesh.dpds.model.internals.InternalComponentsDPDS;
import org.opendatamesh.dpds.visitors.DataProductVersionDPDSVisitor;
import org.opendatamesh.dpds.visitors.core.ComponentsDPDSVisitor;
import org.opendatamesh.dpds.visitors.interfaces.InterfaceComponentsDPDSVisitor;
import org.opendatamesh.dpds.visitors.internals.InternalComponentsDPDSVisitor;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class DataProductVersionRefVisitor extends RefVisitor implements DataProductVersionDPDSVisitor {

    public DataProductVersionRefVisitor(ReferenceHandler referenceHandler) {
        super(null);
        this.referenceHandler = referenceHandler;
    }

    @Override
    public void visit(InfoDPDS infoDPDS) {
        // DO NOTHING
    }

    @Override
    public void visit(InterfaceComponentsDPDS interfaceComponentsDPDS) {
        InterfaceComponentsDPDSVisitor visitor = new InterfaceComponentsRefVisitor(this);
        Stream.of(
                        interfaceComponentsDPDS.getInputPorts(),
                        interfaceComponentsDPDS.getOutputPorts(),
                        interfaceComponentsDPDS.getDiscoveryPorts(),
                        interfaceComponentsDPDS.getObservabilityPorts(),
                        interfaceComponentsDPDS.getControlPorts()
                )
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(visitor::visit);
    }

    @Override
    public void visit(InternalComponentsDPDS internalComponentsDPDS) {
        InternalComponentsDPDSVisitor visitor = new InternalComponentsDPDSRefVisitor(this);
        if (internalComponentsDPDS.getApplicationComponents() != null) {
            internalComponentsDPDS.getApplicationComponents().forEach(visitor::visit);
        }
        if (internalComponentsDPDS.getInfrastructuralComponents() != null) {
            internalComponentsDPDS.getInfrastructuralComponents().forEach(visitor::visit);
        }

        visitor.visit(internalComponentsDPDS.getLifecycleInfo());
    }

    @Override
    public void visit(ComponentsDPDS componentsDPDS) {
        ComponentsDPDSVisitor visitor = new ComponentRefVisitor(this);
        Stream.of(
                        componentsDPDS.getInputPorts(),
                        componentsDPDS.getOutputPorts(),
                        componentsDPDS.getDiscoveryPorts(),
                        componentsDPDS.getObservabilityPorts(),
                        componentsDPDS.getControlPorts()
                )
                .filter(Objects::nonNull)
                .flatMap(map -> map.entrySet().stream())
                .forEach(entry -> visitor.visit(entry.getValue()));

        if (componentsDPDS.getApplicationComponents() != null) {
            componentsDPDS.getApplicationComponents().forEach((k, v) -> visitor.visit(v));
        }
        if (componentsDPDS.getInfrastructuralComponents() != null) {
            componentsDPDS.getInfrastructuralComponents().forEach((k, v) -> visitor.visit(v));
        }
        if (componentsDPDS.getApis() != null) {
            componentsDPDS.getApis().forEach((k, v) -> visitor.visit(v));
        }
        if (componentsDPDS.getTemplates() != null) {
            componentsDPDS.getTemplates().forEach((k, v) -> visitor.visit(v));
        }
    }

    @Override
    public void visit(ExternalResourceDPDS externalResourceDPDS) {
        // DO NOTHING
    }
}

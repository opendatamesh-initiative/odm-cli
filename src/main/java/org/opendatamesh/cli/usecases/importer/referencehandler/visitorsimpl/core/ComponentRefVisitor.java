package org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.core;

import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.RefVisitor;
import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.interfaces.PortRefVisitor;
import org.opendatamesh.dpds.model.core.StandardDefinitionDPDS;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;
import org.opendatamesh.dpds.model.internals.ApplicationComponentDPDS;
import org.opendatamesh.dpds.model.internals.InfrastructuralComponentDPDS;
import org.opendatamesh.dpds.visitors.core.ComponentsDPDSVisitor;
import org.opendatamesh.dpds.visitors.interfaces.PortDPDSVisitor;

public class ComponentRefVisitor extends RefVisitor implements ComponentsDPDSVisitor {
    public ComponentRefVisitor(RefVisitor parent) {
        super(parent);
    }

    @Override
    public void visit(PortDPDS portDPDS) {
        PortDPDSVisitor visitor = new PortRefVisitor(this);
        if (portDPDS.getPromises() != null) {
            visitor.visit(portDPDS.getPromises());
        }
        if (portDPDS.getObligations() != null) {
            visitor.visit(portDPDS.getObligations());
        }
        if (portDPDS.getExpectations() != null) {
            visitor.visit(portDPDS.getExpectations());
        }
    }

    @Override
    public void visit(ApplicationComponentDPDS applicationComponentDPDS) {
        referenceHandler.handleReference(applicationComponentDPDS);
    }

    @Override
    public void visit(InfrastructuralComponentDPDS infrastructuralComponentDPDS) {
        referenceHandler.handleReference(infrastructuralComponentDPDS);
    }

    @Override
    public void visit(StandardDefinitionDPDS standardDefinitionDPDS) {
        referenceHandler.handleReference(standardDefinitionDPDS);
        referenceHandler.handleApiDefinitionReference(standardDefinitionDPDS);
    }
}

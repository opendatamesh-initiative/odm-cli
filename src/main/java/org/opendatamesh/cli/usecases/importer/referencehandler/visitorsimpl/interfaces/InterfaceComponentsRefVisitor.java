package org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.interfaces;

import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.RefVisitor;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;
import org.opendatamesh.dpds.visitors.interfaces.InterfaceComponentsDPDSVisitor;
import org.opendatamesh.dpds.visitors.interfaces.PortDPDSVisitor;

public class InterfaceComponentsRefVisitor extends RefVisitor implements InterfaceComponentsDPDSVisitor {
    public InterfaceComponentsRefVisitor(RefVisitor parent) {
        super(parent);
    }

    @Override
    public void visit(PortDPDS portDPDS) {
        referenceHandler.handleReference(portDPDS);

        PortDPDSVisitor visitor = new PortRefVisitor(this);
        if (portDPDS.getPromises() != null) {
            visitor.visit(portDPDS.getPromises());
        }
        if (portDPDS.getExpectations() != null) {
            visitor.visit(portDPDS.getExpectations());
        }
        if (portDPDS.getObligations() != null) {
            visitor.visit(portDPDS.getObligations());
        }
    }
}

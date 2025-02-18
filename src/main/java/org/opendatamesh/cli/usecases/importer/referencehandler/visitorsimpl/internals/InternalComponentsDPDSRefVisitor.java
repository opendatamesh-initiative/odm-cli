package org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.internals;

import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.RefVisitor;
import org.opendatamesh.dpds.model.internals.ApplicationComponentDPDS;
import org.opendatamesh.dpds.model.internals.InfrastructuralComponentDPDS;
import org.opendatamesh.dpds.model.internals.LifecycleInfoDPDS;
import org.opendatamesh.dpds.visitors.internals.InternalComponentsDPDSVisitor;

public class InternalComponentsDPDSRefVisitor extends RefVisitor implements InternalComponentsDPDSVisitor {
    public InternalComponentsDPDSRefVisitor(RefVisitor parent) {
        super(parent);
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
    public void visit(LifecycleInfoDPDS lifecycleInfoDPDS) {
        //DO NOTHING
    }
}

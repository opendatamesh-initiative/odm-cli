package org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.interfaces;

import org.opendatamesh.cli.usecases.importer.referencehandler.visitorsimpl.RefVisitor;
import org.opendatamesh.dpds.model.core.ExternalResourceDPDS;
import org.opendatamesh.dpds.model.interfaces.ExpectationsDPDS;
import org.opendatamesh.dpds.model.interfaces.ObligationsDPDS;
import org.opendatamesh.dpds.model.interfaces.PromisesDPDS;
import org.opendatamesh.dpds.visitors.interfaces.PortDPDSVisitor;

public class PortRefVisitor extends RefVisitor implements PortDPDSVisitor {
    public PortRefVisitor(RefVisitor parent) {
        super(parent);
    }

    @Override
    public void visit(ObligationsDPDS obligationsDPDS) {
        //DO NOTHING
    }

    @Override
    public void visit(ExpectationsDPDS expectationsDPDS) {
        //DO NOTHING
    }

    @Override
    public void visit(PromisesDPDS promisesDPDS) {
        referenceHandler.handleReference(promisesDPDS.getApi());
        if (promisesDPDS.getApi() != null) {
            referenceHandler.handleApiDefinitionReference(promisesDPDS.getApi());
        }
    }

    @Override
    public void visit(ExternalResourceDPDS externalResourceDPDS) {
        //DO NOTHING
    }
}

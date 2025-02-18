package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;

class PortImporterParameterOutboundPortMock implements PortImporterParameterOutboundPort {

    private final ImporterArguments importSchemaArguments;

    PortImporterParameterOutboundPortMock(ImporterArguments importSchemaArguments) {
        this.importSchemaArguments = importSchemaArguments;
    }

    @Override
    public ImporterArguments getImporterArguments() {
        return importSchemaArguments;
    }
}

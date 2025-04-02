package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.importer.ImporterArguments;

class ImporterParameterOutboundPortMock implements ImporterParameterOutboundPort {

    private final ImporterArguments importSchemaArguments;

    ImporterParameterOutboundPortMock(ImporterArguments importSchemaArguments) {
        this.importSchemaArguments = importSchemaArguments;
    }

    @Override
    public ImporterArguments getImporterArguments() {
        return importSchemaArguments;
    }
}

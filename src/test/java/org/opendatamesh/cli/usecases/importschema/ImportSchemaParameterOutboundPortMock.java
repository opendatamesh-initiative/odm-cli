package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaArguments;

class ImportSchemaParameterOutboundPortMock implements ImportSchemaParameterOutboundPort {

    private final ImportSchemaArguments importSchemaArguments;

    ImportSchemaParameterOutboundPortMock(ImportSchemaArguments importSchemaArguments) {
        this.importSchemaArguments = importSchemaArguments;
    }

    @Override
    public ImportSchemaArguments getImportSchemaArguments() {
        return importSchemaArguments;
    }
}

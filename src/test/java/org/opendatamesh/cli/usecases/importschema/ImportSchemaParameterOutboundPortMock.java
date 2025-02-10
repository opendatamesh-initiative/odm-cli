package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaArguments;

class ImportSchemaParameterOutboundPortMock implements ImportSchemaParameterOutboundPort {

    @Override
    public ImportSchemaArguments getImportSchemaArguments() {
        return new ImportSchemaArguments();
    }
}

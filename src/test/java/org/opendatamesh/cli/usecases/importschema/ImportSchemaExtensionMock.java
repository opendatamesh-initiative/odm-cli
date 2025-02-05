package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaOptions;
import org.opendatamesh.dpds.model.core.EntityDPDS;


class ImportSchemaExtensionMock implements ImportSchemaExtension {

    private final ImportSchemaExtensionMockState state;

    public ImportSchemaExtensionMock(ImportSchemaExtensionMockState state) {
        this.state = state;
    }

    @Override
    public boolean supports(String from, String to) {
        return true;
    }

    @Override
    public <T extends EntityDPDS> T importElement(ImportSchemaOptions importSchemaOptions) {
        return (T) state.getOutputPort();
    }
}

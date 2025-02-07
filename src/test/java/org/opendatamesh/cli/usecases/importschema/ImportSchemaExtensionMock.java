package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.extensions.ExtensionInfo;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaArguments;
import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.opendatamesh.dpds.model.core.EntityDPDS;

import java.util.List;


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
    public List<ExtensionOption> getExtensionOptions() {
        return List.of();
    }

    @Override
    public ExtensionInfo getExtensionInfo() {
        return new ExtensionInfo.Builder()
                .description("Test")
                .build();
    }

    @Override
    public <T extends EntityDPDS> T importElement(ImportSchemaArguments importSchemaArguments) {
        return (T) state.getOutputPort();
    }
}

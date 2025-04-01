package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.ExtensionInfo;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.dpds.model.interfaces.Port;

import java.util.List;


class PortImporterExtensionMock implements ImporterExtension<Port> {

    private final PortImporterExtensionMockState state;

    public PortImporterExtensionMock(PortImporterExtensionMockState state) {
        this.state = state;
    }

    @Override
    public boolean supports(String from, String to) {
        return true;
    }

    @Override
    public Port importElement(Port t, ImporterArguments importerArguments) {
        return state.getOutputPort();
    }

    @Override
    public Class getTargetClass() {
        return Port.class;
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

}

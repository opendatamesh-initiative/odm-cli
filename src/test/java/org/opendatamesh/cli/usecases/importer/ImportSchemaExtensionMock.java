package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.ExtensionInfo;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.dpds.model.interfaces.PortDPDS;

import java.util.List;


class ImporterExtensionMock implements ImporterExtension<PortDPDS> {

    private final ImporterExtensionMockState state;

    public ImporterExtensionMock(ImporterExtensionMockState state) {
        this.state = state;
    }

    @Override
    public boolean supports(String from, String to) {
        return true;
    }

    @Override
    public PortDPDS importElement(PortDPDS t, ImporterArguments importerArguments) {
        return state.getOutputPort();
    }

    @Override
    public Class getTargetClass() {
        return PortDPDS.class;
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

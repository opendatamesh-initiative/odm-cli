package org.opendatamesh.cli.usecases.importer;

import org.opendatamesh.cli.extensions.ExtensionInfo;
import org.opendatamesh.cli.extensions.ExtensionOption;
import org.opendatamesh.cli.extensions.importer.ImporterArguments;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.opendatamesh.dpds.model.DataProductVersion;

import java.util.List;

public class DescriptorImporterExtensionMock implements ImporterExtension<DataProductVersion> {

    private final DescriptorImporterExtensionMockState state;

    public DescriptorImporterExtensionMock(DescriptorImporterExtensionMockState state) {
        this.state = state;
    }

    @Override
    public boolean supports(String s, String s1) {
        return true;
    }

    @Override
    public DataProductVersion importElement(DataProductVersion dataProductVersion, ImporterArguments importerArguments) {
        return state.dataProductVersion;
    }

    @Override
    public Class<DataProductVersion> getTargetClass() {
        return DataProductVersion.class;
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

package org.opendatamesh.cli.commands;

import org.opendatamesh.cli.configs.OdmCliConfiguration;

public class OdmCliRootCommandExecutor extends PicoCliCommandExecutor {

    protected OdmCliRootCommandExecutor(OdmCliConfiguration odmCliConfiguration) {
        super(odmCliConfiguration);
    }

    @Override
    protected void handleRequiredOptions(Boolean interactive) {
        if (interactive == null) {
            throw new IllegalStateException("Interactive option has no value assigned.");
        }
    }
}

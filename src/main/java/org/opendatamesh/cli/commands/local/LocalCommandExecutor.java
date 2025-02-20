package org.opendatamesh.cli.commands.local;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.springframework.stereotype.Component;

@Component
public class LocalCommandExecutor extends PicoCliCommandExecutor {

    protected LocalCommandExecutor(OdmCliConfiguration odmCliConfiguration) {
        super(odmCliConfiguration);
    }
}

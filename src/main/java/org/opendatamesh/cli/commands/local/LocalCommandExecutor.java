package org.opendatamesh.cli.commands.local;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.springframework.stereotype.Component;

@Component
public class LocalCommandExecutor implements PicoCliCommandExecutor {

    @Override
    public Integer call() {
        return 0;
    }
}

package org.opendatamesh.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public abstract class PicoCliCommandExecutor implements Callable<Integer> {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public final Integer call() {
        try {
            handleRequiredOptions();
            executeUseCase();
        } catch (Exception e) {
            log.error("Failed to execute command, cause: {}", e.getMessage());
        }
        return 0;
    }

    protected void handleRequiredOptions() {

    }

    protected void executeUseCase() {
    }
}

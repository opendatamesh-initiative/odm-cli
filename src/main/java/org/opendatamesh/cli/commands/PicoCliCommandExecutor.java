package org.opendatamesh.cli.commands;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.SOFTWARE;

/**
 * Abstract class representing a command executor for PicoCli-based CLI commands.
 * <p>
 * Extend this class to implement specific command execution logic.
 * The instantiated object should be passed to the {@code CommandSpec} built inside a
 * {@code PicoCliCommandBuilder} like this:
 * </p>
 * <pre>
 * CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
 * </pre>
 */
public abstract class PicoCliCommandExecutor implements Callable<Integer> {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    protected final OdmCliConfiguration odmCliConfiguration;

    protected PicoCliCommandExecutor(OdmCliConfiguration odmCliConfiguration) {
        this.odmCliConfiguration = odmCliConfiguration;
    }

    /**
     * Executes the CLI command when invoked by PicoCli.
     *
     * @return an integer status code (0 for success, non-zero for errors)
     */
    @Override
    public final Integer call() {
        try {
            handleRequiredOptions(odmCliConfiguration.getCliConfiguration().isInteractive());
            executeUseCase();
            return OK;
        } catch (Exception e) {
            log.error("Failed to execute command, cause: {}", e.getMessage(), e);
            return SOFTWARE;
        }
    }

    /**
     * Handles required options before executing the main command logic.
     * Use utils like {@code CommandOptionsUtils.handleRequiredOptions(List<ExtensionOption> extensionOptions)}
     * <br>to easily handle all the {@link org.opendatamesh.cli.extensions.ExtensionOption}
     * <br>of a {@link  org.opendatamesh.cli.extensions.Extension}
     */
    protected void handleRequiredOptions(Boolean interactive) {

    }

    /**
     * Executes the main logic (use case) of the command.
     */
    protected void executeUseCase() {
    }
}

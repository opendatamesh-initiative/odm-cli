package org.opendatamesh.cli.commands;

import picocli.CommandLine;

/**
 * Interface for building {@link CommandLine}
 * <p>
 * Implementations of this interface should provide mechanisms to construct
 * and configure a {@link CommandLine} instance.
 * </p>
 */
public interface PicoCliCommandBuilder {

    /**
     * Builds a {@link CommandLine} instance using the specified arguments.
     *
     * @param args command-line arguments to be used for building the command
     * @return a configured {@link CommandLine} instance
     */
    CommandLine buildCommand(String... args);

    /**
     * Retrieves the name of the parent command.
     *
     * @return the parent command name as a {@link String}
     */
    String getParentCommandName();

    /**
     * Retrieves the name of the command.
     *
     * @return the command name as a {@link String}
     */
    String getCommandName();
}

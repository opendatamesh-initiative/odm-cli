package org.opendatamesh.cli.commands;

import picocli.CommandLine;

public interface PicoCliCommandBuilder {
    CommandLine buildCommand();

    String getParentCommandName();

    String getCommandName();
}

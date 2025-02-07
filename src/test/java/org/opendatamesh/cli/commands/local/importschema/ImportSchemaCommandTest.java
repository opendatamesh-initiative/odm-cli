package org.opendatamesh.cli.commands.local.importschema;

import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.commands.OdmCliCommandIT;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportSchemaCommandTest extends OdmCliCommandIT {
    @Test
    void testImportSchemaCommand() {
        String[] args = {"odm-cli", "local", "import"};
        CommandLine commandLine = rootCommand.buildCommand(args);

        int exitCode = commandLine.execute(args);
        assertEquals(2, exitCode, "Expected exit code should be 2");
    }
}

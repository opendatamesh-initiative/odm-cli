package org.opendatamesh.cli.commands.local.importschema;

import org.junit.jupiter.api.Disabled;
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

    /*
     * Test method used only for DEBUG purposes
     * */
    @Test
    @Disabled
    void testImportSchemaExtensions() throws Exception {
        String[] args = {"local",
                "-s=NORMALIZED",
                "import",
                "-f" + getClass().getResource("data-product-descriptor.json").getPath(),
                "--from=jdbc",
                "--to=port",
                "--databaseName=postgres",
                "--schemaName=public",
                "--portName=outport",
                "--portVersion=1.0.0"
        };
        invokeCommand(args);
    }
}

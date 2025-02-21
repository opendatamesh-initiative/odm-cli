package org.opendatamesh.cli.commands.local.importer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.commands.OdmCliCommandIT;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class PortImporterCommandTest extends OdmCliCommandIT {
    @Test
    void testImportSchemaCommand() {
        String[] args = {"odm-cli", "local", "import"};
        CommandLine commandLine = rootCommand.buildCommand(args);

        int exitCode = commandLine.execute(args);
        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    void testFailInteractive() throws Exception {
        String[] args = {
                "--interactive=false", //The command must fail if required options are not initialized.
                "local",
                "-s=NORMALIZED",
                "import",
                "-f" + getClass().getResource("data-product-descriptor.json").getPath(),
                "--from=starter",
                "--source=starter",
                "--to=output-port",
                "--target=port_name",

        };
        assertThatThrownBy(() -> invokeCommand(args))
                .hasMessageStartingWith("Failed command execution: ")
                .isInstanceOf(RuntimeException.class);
    }

    /*
     * Test method used only for DEBUG purposes
     * */
    @Test
    @Disabled
    void testImporterExtensions() throws Exception {
        String[] args = {
                "local",
                "-s=NORMALIZED",
                "import",
                "-f" + getClass().getResource("data-product-descriptor.json").getPath(),
                "--from=starter",
                "--source=starter",
                "--to=output-port",
                "--target=port_name",
                "--databaseName=postgres",
                "--schemaName=public"
        };
        invokeCommand(args);
    }
}

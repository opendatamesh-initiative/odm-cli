package org.opendatamesh.cli.commands.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.commands.OdmCliCommandIT;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigCommandTest extends OdmCliCommandIT {
    @Test
    void testConfigCommand() {
        String[] args = {"odm-cli", "config"};
        CommandLine commandLine = rootCommand.buildCommand(args);

        int exitCode = commandLine.execute(args);
        assertThat(exitCode).isEqualTo(2);
    }

    /*
     * Test method used only for DEBUG purposes
     * */
    @Test
    @Disabled
    void testConfig() throws Exception {
        String[] args = {
                "config",
                "--set",
                "cli.env.prova=ciao",
                "--unset",
                "cli.env.custom",
                "--setarray", "cli.env.array", "fieldA.fieldB.pluto=value1",
                "--unsetarray", "cli.systems", "name=testDb",
        };
        invokeCommand(args);
    }
}

package org.opendatamesh.cli.commands.local.init;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.commands.OdmCliCommandIT;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class InitCommandTest extends OdmCliCommandIT {

    @Test
    @Disabled
    void testFailInteractive() {
        String[] args = {
                "--interactive=false", //The command must fail if required options are not initialized.
                "local",
                "-s=NORMALIZED",
                "init",
                "--domain=testDomain",
                "--name=testName",
                "--force",
                "--outputFile=~/git/odm-cli/test.json"

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
    void testInitExtensions() throws Exception {
        String[] args = {
                "local",
                "-s=NORMALIZED",
                "init",
                "--domain=testDomain",
                "--name=testName",
        };
        invokeCommand(args);
    }
}

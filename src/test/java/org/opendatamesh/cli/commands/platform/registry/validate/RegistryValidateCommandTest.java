package org.opendatamesh.cli.commands.platform.registry.validate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.commands.OdmCliCommandIT;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryValidateCommandTest extends OdmCliCommandIT {

    @Test
    void testImportSchemaCommand() {
        String[] args = {"odm-cli", "platform", "registry", "validate"};
        CommandLine commandLine = rootCommand.buildCommand(args);

        int exitCode = commandLine.execute(args);
        assertThat(exitCode).isEqualTo(2);
    }

    @Disabled
    @Test
    void testRegistryValidateCommand() throws Exception {
        String[] args = {
                "--interactive=false",
                "platform",
                "registry",
                "validate",
                "-f" + getClass().getResource("data-product-descriptor.json").getPath(),
                "--verbose=true"
        };
        invokeCommand(args);
    }

}

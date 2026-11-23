package org.opendatamesh.cli.commands;

import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.OdmCliApplication;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ExitCodeCommandTest extends OdmCliCommandIT {

    @Autowired
    private OdmCliApplication application;

    @Test
    void testSuccessfulCommandExitsWithZero() {
        application.run("--version");
        assertThat(application.getExitCode()).isZero();
    }

    @Test
    void testFailingSubcommandExitsWithSoftwareError() {
        application.run("--interactive=false", "platform", "registry", "validate", "-f", "nope.json");
        assertThat(application.getExitCode()).isEqualTo(1);
    }

    @Test
    void testInvalidUsageExitsWithUsageError() {
        application.run("local", "import");
        assertThat(application.getExitCode()).isEqualTo(2);
    }
}

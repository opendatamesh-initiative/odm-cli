package org.opendatamesh.cli.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionCommandTest extends OdmCliCommandIT {

    @Autowired
    private BuildProperties buildProperties;

    @Test
    void testRootVersionIsTheBuildVersion() {
        assertThat(printVersion("--version")).isEqualTo(buildProperties.getVersion());
    }

    @Test
    void testConfigVersionIsTheBuildVersion() {
        assertThat(printVersion("config", "--version")).isEqualTo("odm-cli config " + buildProperties.getVersion());
    }

    @Test
    void testSubcommandVersionIsTheBuildVersion() {
        assertThat(printVersion("platform", "registry", "validate", "--version")).isEqualTo(buildProperties.getVersion());
    }

    private String printVersion(String... args) {
        CommandLine commandLine = rootCommand.buildCommand(args);
        StringWriter output = new StringWriter();
        commandLine.setOut(new PrintWriter(output));
        int exitCode = commandLine.execute(args);
        assertThat(exitCode).isZero();
        return output.toString().trim();
    }
}

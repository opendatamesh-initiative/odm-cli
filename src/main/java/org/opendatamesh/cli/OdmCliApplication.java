package org.opendatamesh.cli;

import org.opendatamesh.cli.commands.OdmCliRootCommandBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import picocli.CommandLine;

@SpringBootApplication
@EnableAsync
public class OdmCliApplication implements CommandLineRunner, ExitCodeGenerator {

    @Autowired
    private OdmCliRootCommandBuilder rootCommand;

    private int exitCode;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OdmCliApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        //The exit code of the executed command becomes the exit code of the process
        System.exit(SpringApplication.exit(app.run(args)));
    }

    @Override
    public void run(String... args) {
        CommandLine odmCliCommand = rootCommand.buildCommand(args);
        odmCliCommand.setExecutionStrategy(new CommandLine.RunAll());
        exitCode = odmCliCommand.execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}

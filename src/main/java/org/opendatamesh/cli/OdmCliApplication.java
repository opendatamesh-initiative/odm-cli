package org.opendatamesh.cli;

import org.opendatamesh.cli.commands.OdmCliRootCommandBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
public class OdmCliApplication implements CommandLineRunner {

    @Autowired
    private OdmCliRootCommandBuilder rootCommand;

    public static void main(String[] args) {
        SpringApplication.run(OdmCliApplication.class, args);
    }

    @Override
    public void run(String... args) {
        CommandLine odmCliCommand = rootCommand.buildCommand(args);
        odmCliCommand.setExecutionStrategy(new CommandLine.RunAll());
        odmCliCommand.execute(args);
    }
}

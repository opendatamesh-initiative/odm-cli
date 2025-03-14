package org.opendatamesh.cli.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.cli.OdmCliApplication;
import org.opendatamesh.cli.extensions.ExtensionsLoader;
import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import picocli.CommandLine;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static picocli.CommandLine.ExitCode.OK;

@SpringBootTest(classes = {OdmCliApplication.class})
public class OdmCliCommandIT {
    @Autowired
    protected OdmCliRootCommandBuilder rootCommand;

    @MockBean
    private ExtensionsLoader extensionsLoader;

    @BeforeEach
    public void setup() throws Exception {
        File extensionsDir = new File(OdmCliCommandIT.class.getResource("").getFile());
        if (!extensionsDir.exists() || !extensionsDir.isDirectory()) {
            throw new RuntimeException("Directory extensions non found: " + extensionsDir.getAbsolutePath());
        }
        File[] jarFiles = extensionsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            throw new RuntimeException("No .jar found on: " + extensionsDir.getAbsolutePath());
        }

        URL[] jarUrls = new URL[jarFiles.length];
        for (int i = 0; i < jarFiles.length; i++) {
            jarUrls[i] = jarFiles[i].toURI().toURL();
        }

        URLClassLoader jarClassLoader = new URLClassLoader(jarUrls, this.getClass().getClassLoader());

        ServiceLoader<ImporterExtension> serviceLoader = ServiceLoader.load(ImporterExtension.class, jarClassLoader);

        Mockito.when(extensionsLoader.getImporterExtension(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String from = invocation.getArgument(0);
                    String to = invocation.getArgument(1);
                    return serviceLoader.stream()
                            .map(ServiceLoader.Provider::get)
                            .filter(extension -> extension.supports(from, to))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    String.format("No ImporterExtension found supporting from '%s' to '%s'", from, to)
                            ));
                });
    }

    /**
     * if this test fails check src/test/resources/org/opendatamesh/cli/commands/odm-cli-extensions-starter.jar
     */
    @Test
    public void testExtensionLoading() {
        ImporterExtension extension = extensionsLoader.getImporterExtension("starter", "port");
        assertNotNull(extension, "The extension should not be null");
        System.out.println("Loaded extension: " + extension.getClass().getName());
    }

    protected void invokeCommand(String... args) throws Exception {
        CommandLine commandLine = rootCommand.buildCommand(args);

        CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
        List<Object> commandObjects = parseResult.asCommandLineList().stream()
                .map(CommandLine::getCommand)
                .collect(Collectors.toList());

        Assertions.assertFalse(commandObjects.isEmpty(), "No commands were parsed.");

        for (Object command : commandObjects) {
            if (command instanceof Callable) {
                System.out.println("Invoking call() on: " + command.getClass().getName());
                Integer result = ((Callable<Integer>) command).call();
                System.out.println("Result: " + result);
                if (result != OK) {
                    throw new RuntimeException("Failed command execution: " + command.getClass());
                }
            } else if (command instanceof Runnable) {
                System.out.println("Invoking run() on: " + command.getClass().getName());
                ((Runnable) command).run();
            } else {
                System.out.println("Command " + command.getClass().getName() +
                        " does not implement Callable or Runnable. Skipping.");
            }
        }
    }

}

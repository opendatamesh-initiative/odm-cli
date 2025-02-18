package org.opendatamesh.cli.extensions;

import org.opendatamesh.cli.extensions.importer.ImporterExtension;
import org.springframework.stereotype.Component;

import java.util.ServiceLoader;

@Component
public class ExtensionsLoader {

    public ImporterExtension getImporterExtension(String from, String to) {

        ServiceLoader<ImporterExtension> serviceLoader = ServiceLoader.load(
                ImporterExtension.class
        );

        return serviceLoader.stream().map(ServiceLoader.Provider::get)
                .filter(extension -> extension.supports(from, to))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No ImporterExtension found supporting from '%s' to '%s'", from, to)
                ));
    }
}

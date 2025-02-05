package org.opendatamesh.cli.extensions;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.springframework.stereotype.Component;

import java.util.ServiceLoader;

@Component
public class ExtensionsLoader {

    public ImportSchemaExtension getImportSchemaExtension(String from, String to) {

        ServiceLoader<ImportSchemaExtension> serviceLoader = ServiceLoader.load(
                ImportSchemaExtension.class
        );

        return serviceLoader.stream().map(ServiceLoader.Provider::get)
                .filter(extension -> extension.supports(from, to))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No ImportSchemaExtension found supporting from '%s' to '%s'", from, to)
                ));
    }
}

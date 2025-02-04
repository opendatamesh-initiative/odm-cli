package org.opendatamesh.cli.extensions;

import org.opendatamesh.cli.extensions.importschema.ImportSchemaExtension;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExtensionsLoader {

    public ImportSchemaExtension getImportSchemaExtension(String from, String to) {

        List<ImportSchemaExtension> plugins = SpringFactoriesLoader.loadFactories(
                ImportSchemaExtension.class,
                this.getClass().getClassLoader()
        );

        return plugins.stream().filter(plugin -> plugin.supports(from, to)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No ImportPlugin found supporting from '%s' to '%s'", from, to)
                ));
    }
}

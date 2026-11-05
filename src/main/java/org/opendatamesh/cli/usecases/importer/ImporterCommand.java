package org.opendatamesh.cli.usecases.importer;

import java.util.Map;

/**
 * Input shared by the importers (DescriptorImporter and PortImporter), dispatched by {@link ImporterFactory}.
 *
 * @param descriptorFilePath    path of the data product descriptor to update
 * @param to                    the import target type (e.g. "descriptor", "output-port")
 * @param target                the import target name (e.g. the name of the port)
 * @param parentCommandOptions  all the options of the import command, handed over to the importer extension
 */
public record ImporterCommand(
        String descriptorFilePath,
        String to,
        String target,
        Map<String, String> parentCommandOptions
) {
}

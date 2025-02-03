package org.opendatamesh.cli.usecases.importschema;

import java.nio.file.Path;
import java.util.Map;

interface ImportSchemaParameterOutboundPort {
    Path getDescriptorPath();

    String getFrom();

    String getTo();

    Map<String, String> getInParams();

    Map<String, String> getOutParams();
}

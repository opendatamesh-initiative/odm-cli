package org.opendatamesh.cli.usecases.config.reader;

import com.fasterxml.jackson.databind.JsonNode;

interface ConfigReaderPersistenceOutboundPort {
    JsonNode getConfigContent();
}

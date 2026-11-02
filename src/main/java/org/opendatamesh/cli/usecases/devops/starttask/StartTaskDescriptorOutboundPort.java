package org.opendatamesh.cli.usecases.devops.starttask;

import com.fasterxml.jackson.databind.JsonNode;

interface StartTaskDescriptorOutboundPort {
    DescriptorLifecycle readLifecycle(JsonNode content);
}

package org.opendatamesh.cli.usecases.devops.starttask;

import java.util.Map;

/** One task of a lifecycleInfo stage, as written in the descriptor. */
record DescriptorTask(
        String name,
        String templateName,
        String templateDescription,
        String executorRef,
        Integer order,
        Map<String, String> params
) {
}

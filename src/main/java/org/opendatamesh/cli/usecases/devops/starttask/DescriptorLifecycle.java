package org.opendatamesh.cli.usecases.devops.starttask;

import java.util.LinkedHashMap;
import java.util.List;

/** The descriptor version and its lifecycleInfo stages, in descriptor key order. */
record DescriptorLifecycle(String descriptorVersion, LinkedHashMap<String, List<DescriptorTask>> stages) {
}

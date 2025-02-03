package org.opendatamesh.cli.plugin;

import org.opendatamesh.dpds.model.core.EntityDPDS;

import java.nio.file.Path;
import java.util.Map;

public interface ImportPlugin {
    boolean supports(String from, String to);

    <T extends EntityDPDS> T importElement(Path rootDescriptorPath, Map<String, String> outParamMap);
}

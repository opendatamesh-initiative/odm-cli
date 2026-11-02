package org.opendatamesh.cli.clients.platform.devops.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DevOpsActivityRes(
        String uuid,
        String dataProductVersionUuid,
        String dataProductFqn,
        String dataProductVersionTag,
        String name,
        Integer sortOrder,
        String status,
        List<DevOpsTaskRes> tasks
) {
}

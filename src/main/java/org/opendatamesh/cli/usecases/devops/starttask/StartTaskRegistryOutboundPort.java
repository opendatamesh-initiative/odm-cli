package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.registry.RegistryDataProduct;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersion;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersionSummary;

import java.util.List;

interface StartTaskRegistryOutboundPort {
    List<RegistryDataProduct> findDataProductsByFqn(String fqn);

    List<RegistryDataProductVersionSummary> findDataProductVersions(String dataProductUuid, String versionNumber);

    RegistryDataProductVersion getDataProductVersion(String uuid);
}

package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.registry.PlatformRegistryClient;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProduct;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersion;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersionSummary;

import java.util.List;
import java.util.function.Supplier;

/** Registry is reached only when no open activity exists, so its client is created on first use. */
class StartTaskRegistryOutboundPortImpl implements StartTaskRegistryOutboundPort {

    private final Supplier<PlatformRegistryClient> clientSupplier;
    private PlatformRegistryClient client;

    StartTaskRegistryOutboundPortImpl(Supplier<PlatformRegistryClient> clientSupplier) {
        this.clientSupplier = clientSupplier;
    }

    /** Created once per command run, so an OAuth2 token is not requested for each of the three calls. */
    private PlatformRegistryClient registryClient() {
        if (client == null) {
            client = clientSupplier.get();
        }
        return client;
    }

    @Override
    public List<RegistryDataProduct> findDataProductsByFqn(String fqn) {
        return registryClient().findDataProductsByFqn(fqn);
    }

    @Override
    public List<RegistryDataProductVersionSummary> findDataProductVersions(String dataProductUuid, String versionNumber) {
        return registryClient().findDataProductVersions(dataProductUuid, versionNumber);
    }

    @Override
    public RegistryDataProductVersion getDataProductVersion(String uuid) {
        return registryClient().getDataProductVersion(uuid);
    }
}

package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.registry.RegistryDataProduct;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersion;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersionSummary;
import org.opendatamesh.cli.clients.platform.registry.RegistryValidationState;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.List;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.CONFLICT;
import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.INVALID;
import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.NOT_FOUND;

/** The data product version published in Registry that start-task turns into an activity. */
class VersionLookup {

    private final StartTaskRegistryOutboundPort registryPort;
    private final StartTaskPresenter presenter;

    VersionLookup(StartTaskRegistryOutboundPort registryPort, StartTaskPresenter presenter) {
        this.registryPort = registryPort;
        this.presenter = presenter;
    }

    RegistryDataProductVersion publishedVersion(String fqn, String versionNumber, boolean forceStart) {
        RegistryDataProduct product = uniqueProduct(fqn);
        RegistryDataProductVersionSummary summary = uniqueVersion(product, fqn, versionNumber);
        RegistryDataProductVersion version = registryPort.getDataProductVersion(summary.uuid());
        checkPublication(version, fqn, versionNumber, forceStart);
        checkDescriptorContent(version, versionNumber);
        return version;
    }

    private RegistryDataProduct uniqueProduct(String fqn) {
        List<RegistryDataProduct> products = registryPort.findDataProductsByFqn(fqn);
        if (products.isEmpty()) {
            throw productNotFound(fqn);
        }
        if (products.size() > 1) {
            throw productAmbiguous(fqn);
        }
        return products.get(0);
    }

    private RegistryDataProductVersionSummary uniqueVersion(RegistryDataProduct product, String fqn, String versionNumber) {
        List<RegistryDataProductVersionSummary> versions = registryPort.findDataProductVersions(product.uuid(), versionNumber);
        if (versions.isEmpty()) {
            throw versionNotFound(fqn, versionNumber);
        }
        if (versions.size() > 1) {
            throw versionAmbiguous(fqn, versionNumber);
        }
        return versions.get(0);
    }

    private void checkPublication(RegistryDataProductVersion version, String fqn, String versionNumber, boolean forceStart) {
        if (version.validationState() == RegistryValidationState.APPROVED) {
            return;
        }
        if (!forceStart) {
            throw notApproved(fqn, versionNumber, version.validationState());
        }
        presenter.presentWarning("--force-start: running version " + versionNumber + " of " + fqn
                + " in state " + version.validationState());
    }

    private void checkDescriptorContent(RegistryDataProductVersion version, String versionNumber) {
        if (version.content() == null || version.content().isNull() || version.content().isMissingNode()) {
            throw noDescriptorContent(versionNumber);
        }
    }

    private UseCaseException noDescriptorContent(String versionNumber) {
        return new UseCaseException(INVALID, "Version " + versionNumber + " has no descriptor content");
    }

    private UseCaseException productNotFound(String fqn) {
        return new UseCaseException(NOT_FOUND, "Data product " + fqn + " not found in Registry");
    }

    private UseCaseException productAmbiguous(String fqn) {
        return new UseCaseException(CONFLICT, "More than one data product matches " + fqn + " in Registry");
    }

    private UseCaseException versionNotFound(String fqn, String versionNumber) {
        return new UseCaseException(NOT_FOUND, "Version " + versionNumber + " of data product " + fqn + " not found in Registry");
    }

    private UseCaseException versionAmbiguous(String fqn, String versionNumber) {
        return new UseCaseException(CONFLICT, "More than one version " + versionNumber + " of data product " + fqn + " in Registry");
    }

    private UseCaseException notApproved(String fqn, String versionNumber, RegistryValidationState state) {
        return new UseCaseException(INVALID, "Version " + versionNumber + " of " + fqn + " is " + state
                + " in Registry, not APPROVED. Pass --force-start to run it anyway.");
    }
}

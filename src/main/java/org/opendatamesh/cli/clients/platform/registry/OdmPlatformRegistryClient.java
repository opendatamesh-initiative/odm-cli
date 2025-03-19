package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;

public interface OdmPlatformRegistryClient {
    OdmDataProductValidationResponseResource validateDataProduct(OdmDataProductValidationRequestResource validationRequest);
}

package org.opendatamesh.cli.clients.platform.registry;

import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;

public interface PlatformRegistryClient {
    OdmDataProductValidationResponseResource validateDataProduct(OdmDataProductValidationRequestResource validationRequest);
}

package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationRequestResource;
import org.opendatamesh.cli.clients.platform.registry.resources.OdmDataProductValidationResponseResource;
import org.opendatamesh.cli.clients.platform.registry.resources.RegistryDataProductRes;
import org.opendatamesh.cli.clients.platform.registry.resources.RegistryDataProductSearchOptions;
import org.opendatamesh.cli.clients.platform.registry.resources.RegistryDataProductVersionRes;
import org.opendatamesh.cli.clients.platform.registry.resources.RegistryDataProductVersionSearchOptions;
import org.opendatamesh.cli.clients.platform.registry.resources.RegistryDataProductVersionShortRes;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.BadRequestException;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.NotFoundException;
import org.opendatamesh.cli.exceptions.ResourceConflictException;
import org.opendatamesh.cli.exceptions.client.ClientException;
import org.opendatamesh.cli.exceptions.client.ClientResourceMappingException;
import org.opendatamesh.cli.utils.client.RestUtils;
import org.opendatamesh.cli.utils.client.RestUtilsFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

class PlatformRegistryClientImpl implements PlatformRegistryClient {

    static final String V1_ROUTE = "/api/v1/pp/registry";
    static final String V2_ROUTE = "/api/v2/pp/registry";
    private static final ObjectMapper ERROR_MAPPER = new ObjectMapper();

    private final RestUtils restUtils;
    private final PlatformServiceCredentials registryServiceCredential;

    PlatformRegistryClientImpl(
            RestTemplate restTemplate,
            PlatformServiceCredentials registryService
    ) {
        this.restUtils = RestUtilsFactory.getAuthenticatedRestUtils(
                restTemplate,
                registryService.getHeaders(),
                registryService.getOauth2()
        );
        this.registryServiceCredential = registryService;
    }

    @Override
    public DataProductValidationResults validateDataProduct(JsonNode dataProductDescriptor, List<String> policyEvents) {
        OdmDataProductValidationResponseResource response = postValidationReport(toValidationRequest(dataProductDescriptor, policyEvents));
        return toValidationResults(response);
    }

    @Override
    public List<RegistryDataProduct> findDataProductsByFqn(String fqn) {
        return call(() -> restUtils.getPage(
                        registryServiceCredential.getUrl() + V2_ROUTE + "/products",
                        null,
                        Pageable.ofSize(2),
                        new RegistryDataProductSearchOptions(fqn),
                        RegistryDataProductRes.class
                ).getContent().stream()
                .map(res -> new RegistryDataProduct(res.uuid(), res.fqn()))
                .toList());
    }

    @Override
    public List<RegistryDataProductVersionSummary> findDataProductVersions(String dataProductUuid, String versionNumber) {
        return call(() -> restUtils.getPage(
                        registryServiceCredential.getUrl() + V2_ROUTE + "/products-versions",
                        null,
                        Pageable.ofSize(2),
                        new RegistryDataProductVersionSearchOptions(dataProductUuid, versionNumber),
                        RegistryDataProductVersionShortRes.class
                ).getContent().stream()
                .map(res -> new RegistryDataProductVersionSummary(res.uuid(), res.versionNumber()))
                .toList());
    }

    @Override
    public RegistryDataProductVersion getDataProductVersion(String uuid) {
        RegistryDataProductVersionRes res = call(() -> restUtils.get(
                registryServiceCredential.getUrl() + V2_ROUTE + "/products-versions/{id}",
                null,
                uuid,
                RegistryDataProductVersionRes.class
        ));
        return new RegistryDataProductVersion(res.uuid(), res.versionNumber(), toValidationState(res.validationState()), res.content());
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (ClientException e) {
            throw translate(e);
        } catch (ClientResourceMappingException e) {
            throw translate(e);
        }
    }

    private OdmDataProductValidationResponseResource postValidationReport(OdmDataProductValidationRequestResource validationRequest) {
        try {
            return restUtils.genericPost(
                    registryServiceCredential.getUrl() + V1_ROUTE + "/validate/report",
                    null,
                    validationRequest,
                    OdmDataProductValidationResponseResource.class
            );
        } catch (ClientException e) {
            throw new InternalException("ODM Platform registry service failed to validate the data product: " + e.getMessage(), e);
        } catch (ClientResourceMappingException e) {
            throw new InternalException("Unable to read the ODM Platform registry service validation response: " + e.getMessage(), e);
        }
    }

    private OdmDataProductValidationRequestResource toValidationRequest(JsonNode dataProductDescriptor, List<String> policyEvents) {
        OdmDataProductValidationRequestResource validationRequest = new OdmDataProductValidationRequestResource();
        validationRequest.setValidatePolicies(true);
        validationRequest.setValidateSyntax(true);
        validationRequest.setPolicyEventTypes(new ArrayList<>(policyEvents));
        validationRequest.setDataProductVersion(dataProductDescriptor);
        return validationRequest;
    }

    private DataProductValidationResults toValidationResults(OdmDataProductValidationResponseResource response) {
        DataProductValidationResults results = new DataProductValidationResults();
        results.getResults().add(toValidationResult("Data Product Descriptor Syntax", response.getSyntaxValidationResult()));
        response.getPoliciesValidationResults()
                .forEach((policyName, output) -> results.getResults().add(toValidationResult(policyName, output)));
        return results;
    }

    private DataProductValidationResults.Result toValidationResult(String name, OdmDataProductValidationResponseResource.OdmDataProductValidationResult output) {
        return new DataProductValidationResults.Result(
                name,
                output.isValidated(),
                output.getValidationOutput(),
                output.getBlockingFlag()
        );
    }

    private RegistryValidationState toValidationState(String validationState) {
        try {
            return RegistryValidationState.valueOf(validationState);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InternalException("ODM Platform registry service returned an unknown validation state: " + validationState);
        }
    }

    private RuntimeException translate(ClientException e) {
        String serverMessage = serverMessage(e.getResponseBody());
        if (e.getCode() == 409) {
            return new ResourceConflictException(serverMessage != null ? serverMessage
                    : "ODM Platform registry service reported a conflict that persisted after retrying");
        }
        if (serverMessage != null) {
            switch (e.getCode()) {
                case 400:
                    return new BadRequestException(serverMessage);
                case 404:
                    return new NotFoundException(serverMessage);
                default:
                    break;
            }
        }
        if (serverMessage == null && isTransportFailure(e)) {
            return new InternalException("Could not reach the ODM Platform registry service: " + e.getResponseBody().trim());
        }
        String detail = serverMessage != null ? serverMessage : describeUnreadableBody(e.getResponseBody());
        return new InternalException("ODM Platform registry service failed (HTTP " + e.getCode() + "): " + detail);
    }

    private RuntimeException translate(ClientResourceMappingException e) {
        return new InternalException("Unable to read the ODM Platform registry service response: " + e.getMessage());
    }

    private String serverMessage(String responseBody) {
        try {
            JsonNode message = ERROR_MAPPER.readTree(responseBody).get("message");
            return message != null && message.isTextual() && !message.asText().isBlank() ? message.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** The template wraps a transport failure as code 500 with the "I/O error ..." message of the RestClientException. */
    private boolean isTransportFailure(ClientException e) {
        return e.getCode() == 500 && e.getResponseBody() != null && e.getResponseBody().trim().startsWith("I/O error");
    }

    /** A body that is not the platform error JSON is described, never copied: it may be a page from a proxy. */
    private String describeUnreadableBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "no details";
        }
        String trimmed = responseBody.trim();
        String kind = trimmed.startsWith("<") ? "HTML or XML" : "text";
        return "unexpected non-JSON response (" + kind + ", " + trimmed.length() + " characters)";
    }
}

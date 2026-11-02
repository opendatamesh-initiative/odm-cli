package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.NotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class PlatformRegistryClientImplTest {

    private static final String VALIDATE_URL = "http://registry.test/api/v1/pp/registry/validate/report";

    private MockRestServiceServer registryServer;
    private PlatformRegistryClient registryClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        registryServer = MockRestServiceServer.bindTo(restTemplate).build();
        PlatformServiceCredentials credentials = new PlatformServiceCredentials();
        credentials.setUrl("http://registry.test");
        registryClient = new PlatformRegistryClientImpl(restTemplate, credentials);
    }

    @Test
    void testValidateDataProductMapsTheValidationReport() throws Exception {
        JsonNode descriptor = new ObjectMapper().readTree("{\"dataProductDescriptor\":\"1.0.0\"}");
        registryServer.expect(requestTo(VALIDATE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"validateSyntax":true,"validatePolicies":true,"policyEventTypes":[],
                         "dataProductVersion":{"dataProductDescriptor":"1.0.0"}}
                        """, true))
                .andRespond(withSuccess("""
                        {"syntaxValidationResult":{"validated":true,"validationOutput":null,"blockingFlag":true},
                         "policiesValidationResults":{
                           "naming-policy":{"validated":false,"validationOutput":{"reason":"bad name"},"blockingFlag":false}
                         }}
                        """, MediaType.APPLICATION_JSON));

        DataProductValidationResults results = registryClient.validateDataProduct(descriptor, List.of());

        registryServer.verify();
        assertThat(results.getResults()).hasSize(2);
        DataProductValidationResults.Result syntax = results.getResults().get(0);
        assertThat(syntax.getName()).isEqualTo("Data Product Descriptor Syntax");
        assertThat(syntax.isValidated()).isTrue();
        assertThat(syntax.getValidationOutput()).isNull();
        assertThat(syntax.getBlockingFlag()).isTrue();
        DataProductValidationResults.Result policy = results.getResults().get(1);
        assertThat(policy.getName()).isEqualTo("naming-policy");
        assertThat(policy.isValidated()).isFalse();
        assertThat(policy.getValidationOutput()).isEqualTo(Map.of("reason", "bad name"));
        assertThat(policy.getBlockingFlag()).isFalse();
    }

    @Test
    void testValidateDataProductTranslatesRemoteErrors() {
        registryServer.expect(requestTo(VALIDATE_URL))
                .andRespond(withServerError().body("registry down"));

        assertThatThrownBy(() -> registryClient.validateDataProduct(new ObjectMapper().createObjectNode(), List.of()))
                .isInstanceOf(InternalException.class)
                .hasMessageStartingWith("ODM Platform registry service failed to validate the data product: ");
    }

    @Test
    void testValidateDataProductSendsThePolicyEvents() throws Exception {
        JsonNode descriptor = new ObjectMapper().readTree("{\"dataProductDescriptor\":\"1.0.0\"}");
        registryServer.expect(requestTo(VALIDATE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.policyEventTypes").value(contains("DATA_PRODUCT_CREATION")))
                .andRespond(withSuccess("""
                        {"syntaxValidationResult":{"validated":true,"blockingFlag":true},"policiesValidationResults":{}}
                        """, MediaType.APPLICATION_JSON));

        DataProductValidationResults results = registryClient.validateDataProduct(descriptor, List.of("DATA_PRODUCT_CREATION"));

        registryServer.verify();
        assertThat(results.getResults()).hasSize(1);
    }

    private static final String V2 = "http://registry.test/api/v2/pp/registry";

    @Test
    void testFindDataProductsByFqnCallsTheV2SearchWithPageSizeTwo() {
        registryServer.expect(requestTo(V2 + "/products?fqn=urn:dp:sales&page=0&size=2&sort="))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"content":[{"uuid":"p1","fqn":"urn:dp:sales","other":1}],"number":0,"size":2,"totalElements":1}
                        """, MediaType.APPLICATION_JSON));

        List<RegistryDataProduct> products = registryClient.findDataProductsByFqn("urn:dp:sales");

        registryServer.verify();
        assertThat(products).containsExactly(new RegistryDataProduct("p1", "urn:dp:sales"));
    }

    @Test
    void testFindDataProductsByFqnReturnsAnEmptyListForAnEmptyPage() {
        registryServer.expect(requestTo(V2 + "/products?fqn=urn:dp:none&page=0&size=2&sort="))
                .andRespond(withSuccess("{\"content\":[],\"number\":0,\"size\":2,\"totalElements\":0}", MediaType.APPLICATION_JSON));

        assertThat(registryClient.findDataProductsByFqn("urn:dp:none")).isEmpty();
    }

    @Test
    void testFindDataProductVersionsReturnsBothMatchesSoTheCallerCanDetectDuplicates() {
        registryServer.expect(requestTo(V2 + "/products-versions?dataProductUuid=p1&versionNumber=1.2.0&page=0&size=2&sort="))
                .andRespond(withSuccess("""
                        {"content":[{"uuid":"v1","dataProductUuid":"p1","versionNumber":"1.2.0","validationState":"APPROVED"},
                                    {"uuid":"v2","dataProductUuid":"p1","versionNumber":"1.2.0","validationState":"PENDING"}],
                         "number":0,"size":2,"totalElements":2}
                        """, MediaType.APPLICATION_JSON));

        List<RegistryDataProductVersionSummary> versions = registryClient.findDataProductVersions("p1", "1.2.0");

        assertThat(versions).containsExactly(
                new RegistryDataProductVersionSummary("v1", "1.2.0"), new RegistryDataProductVersionSummary("v2", "1.2.0"));
    }

    @Test
    void testGetDataProductVersionMapsTheStateAndTheContent() {
        registryServer.expect(requestTo(V2 + "/products-versions/v1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"uuid":"v1","versionNumber":"1.2.0","validationState":"REJECTED","content":{"dataProductDescriptor":"1.0.0"}}
                        """, MediaType.APPLICATION_JSON));

        RegistryDataProductVersion version = registryClient.getDataProductVersion("v1");

        assertThat(version.uuid()).isEqualTo("v1");
        assertThat(version.versionNumber()).isEqualTo("1.2.0");
        assertThat(version.validationState()).isEqualTo(RegistryValidationState.REJECTED);
        assertThat(version.content().get("dataProductDescriptor").asText()).isEqualTo("1.0.0");
    }

    @Test
    void testGetDataProductVersionTranslates404ToNotFound() {
        registryServer.expect(requestTo(V2 + "/products-versions/missing"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":404,\"message\":\"Version missing not found\"}"));

        assertThatThrownBy(() -> registryClient.getDataProductVersion("missing"))
                .isInstanceOf(NotFoundException.class).hasMessage("Version missing not found");
    }

    @Test
    void testGetDataProductVersionTranslates500ToInternalException() {
        registryServer.expect(requestTo(V2 + "/products-versions/v1")).andRespond(withServerError().body("registry down"));

        assertThatThrownBy(() -> registryClient.getDataProductVersion("v1"))
                .isInstanceOf(InternalException.class)
                .hasMessage("ODM Platform registry service failed (HTTP 500): unexpected non-JSON response (text, 13 characters)");
    }

    @Test
    void testGetDataProductVersionRejectsAnUnknownValidationState() {
        registryServer.expect(requestTo(V2 + "/products-versions/v1"))
                .andRespond(withSuccess("{\"uuid\":\"v1\",\"versionNumber\":\"1.2.0\",\"validationState\":\"UNKNOWN\",\"content\":{}}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> registryClient.getDataProductVersion("v1"))
                .isInstanceOf(InternalException.class)
                .hasMessage("ODM Platform registry service returned an unknown validation state: UNKNOWN");
    }

    @Test
    void testTransportFailureIsReportedAsUnreachable() {
        registryServer.expect(requestTo(V2 + "/products-versions/v1")).andRespond(request -> {
            throw new java.io.IOException("connection refused");
        });

        assertThatThrownBy(() -> registryClient.getDataProductVersion("v1"))
                .isInstanceOf(InternalException.class)
                .hasMessageStartingWith("Could not reach the ODM Platform registry service: I/O error");
    }
}

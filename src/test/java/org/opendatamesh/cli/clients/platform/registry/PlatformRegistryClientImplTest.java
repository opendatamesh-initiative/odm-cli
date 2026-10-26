package org.opendatamesh.cli.clients.platform.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.configs.platform.PlatformServiceCredentials;
import org.opendatamesh.cli.exceptions.InternalException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
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

        DataProductValidationResults results = registryClient.validateDataProduct(descriptor);

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

        assertThatThrownBy(() -> registryClient.validateDataProduct(new ObjectMapper().createObjectNode()))
                .isInstanceOf(InternalException.class)
                .hasMessageStartingWith("ODM Platform registry service failed to validate the data product: ");
    }
}

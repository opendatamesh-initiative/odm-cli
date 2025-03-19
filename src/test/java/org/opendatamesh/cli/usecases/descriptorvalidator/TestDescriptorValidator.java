package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TestDescriptorValidator {
    @Test
    public void testValidateDescriptor() throws IOException {
        // Load JSON descriptor from resource file.
        JsonNode descriptor = new ObjectMapper().readTree(Resources.toByteArray(
                getClass().getResource("test_descriptorvalidator_data_product_descriptor.json")
        ));
        List<String> policyEvents = Lists.newArrayList();
        DataProductValidationResults expectedResults = new DataProductValidationResults();

        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        DescriptorValidatorParametersOutboundPort parametersOutboundPort = mock(DescriptorValidatorParametersOutboundPort.class);

        when(odmOutboundPort.validateDataProduct(descriptor, policyEvents)).thenReturn(expectedResults);
        when(parserOutboundPort.getCanonicalRawDescriptor()).thenReturn(descriptor);
        when(parametersOutboundPort.getPolicyEvents()).thenReturn(policyEvents);

        DescriptorValidator descriptorValidator = new DescriptorValidator(
                odmOutboundPort,
                parserOutboundPort,
                parametersOutboundPort
        );

        DataProductValidationResults results = descriptorValidator.execute();

        assertThat(results)
                .isNotNull()
                .isEqualTo(expectedResults);

        verify(parserOutboundPort, times(1)).getCanonicalRawDescriptor();
        verify(parametersOutboundPort, times(1)).getPolicyEvents();
        verify(odmOutboundPort, times(1)).validateDataProduct(descriptor, policyEvents);
    }
}

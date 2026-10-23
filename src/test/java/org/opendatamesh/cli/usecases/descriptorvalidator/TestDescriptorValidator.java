package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.exceptions.InternalException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        AtomicReference<DataProductValidationResults> presentedResults = new AtomicReference<>();
        DescriptorValidator descriptorValidator = new DescriptorValidator(
                odmOutboundPort,
                parserOutboundPort,
                parametersOutboundPort,
                presentedResults::set
        );

        descriptorValidator.execute();
        DataProductValidationResults results = presentedResults.get();

        assertThat(results)
                .isNotNull()
                .isEqualTo(expectedResults);

        verify(parserOutboundPort, times(1)).getCanonicalRawDescriptor();
        verify(parametersOutboundPort, times(1)).getPolicyEvents();
        verify(odmOutboundPort, times(1)).validateDataProduct(descriptor, policyEvents);
    }

    @Test
    public void testValidateDescriptorFailureIsPresented() {
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        DescriptorValidatorParametersOutboundPort parametersOutboundPort = mock(DescriptorValidatorParametersOutboundPort.class);
        DescriptorValidatorPresenter presenter = mock(DescriptorValidatorPresenter.class, CALLS_REAL_METHODS);

        when(parserOutboundPort.getCanonicalRawDescriptor()).thenThrow(new IllegalStateException("Unreadable descriptor"));

        DescriptorValidator descriptorValidator = new DescriptorValidator(
                odmOutboundPort,
                parserOutboundPort,
                parametersOutboundPort,
                presenter
        );

        assertThatThrownBy(descriptorValidator::execute)
                .isInstanceOf(InternalException.class)
                .hasMessage("Unreadable descriptor");
        verify(presenter, never()).presentValidationResults(any());
        verify(odmOutboundPort, never()).validateDataProduct(any(), any());
    }
}

package org.opendatamesh.cli.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.opendatamesh.cli.clients.platform.registry.DataProductValidationResults;
import org.opendatamesh.cli.exceptions.BadRequestException;
import org.opendatamesh.cli.exceptions.InternalException;
import org.opendatamesh.cli.exceptions.NotFoundException;

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
        List<String> policyEvents = List.of();
        DataProductValidationResults expectedResults = new DataProductValidationResults();

        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);

        when(odmOutboundPort.validateDataProduct(descriptor, policyEvents)).thenReturn(expectedResults);
        when(parserOutboundPort.descriptorExists()).thenReturn(true);
        when(parserOutboundPort.getCanonicalRawDescriptor()).thenReturn(descriptor);

        AtomicReference<DataProductValidationResults> presentedResults = new AtomicReference<>();
        DescriptorValidator descriptorValidator = new DescriptorValidator(
                new DescriptorValidatorCommand("data-product-descriptor.json", null),
                presentedResults::set,
                odmOutboundPort,
                parserOutboundPort
        );

        descriptorValidator.execute();
        DataProductValidationResults results = presentedResults.get();

        assertThat(results)
                .isNotNull()
                .isEqualTo(expectedResults);

        verify(parserOutboundPort, times(1)).getCanonicalRawDescriptor();
        verify(odmOutboundPort, times(1)).validateDataProduct(descriptor, policyEvents);
    }

    @Test
    public void testValidateDescriptorAgainstPolicyEvent() {
        JsonNode descriptor = new ObjectMapper().createObjectNode();
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        when(parserOutboundPort.descriptorExists()).thenReturn(true);
        when(parserOutboundPort.getCanonicalRawDescriptor()).thenReturn(descriptor);

        new DescriptorValidator(
                new DescriptorValidatorCommand("data-product-descriptor.json", "DATA_PRODUCT_CREATION"),
                results -> { },
                odmOutboundPort,
                parserOutboundPort
        ).execute();

        verify(odmOutboundPort, times(1)).validateDataProduct(descriptor, List.of("DATA_PRODUCT_CREATION"));
    }

    @Test
    public void testValidateDescriptorFailureIsPresented() {
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        DescriptorValidatorPresenter presenter = mock(DescriptorValidatorPresenter.class, CALLS_REAL_METHODS);

        when(parserOutboundPort.descriptorExists()).thenReturn(true);
        when(parserOutboundPort.getCanonicalRawDescriptor()).thenThrow(new IllegalStateException("Unreadable descriptor"));

        DescriptorValidator descriptorValidator = new DescriptorValidator(
                new DescriptorValidatorCommand("data-product-descriptor.json", null),
                presenter,
                odmOutboundPort,
                parserOutboundPort
        );

        assertThatThrownBy(descriptorValidator::execute)
                .isInstanceOf(InternalException.class)
                .hasMessage("Unreadable descriptor");
        verify(presenter, never()).presentValidationResults(any());
        verify(odmOutboundPort, never()).validateDataProduct(any(), any());
    }

    @Test
    public void testMissingDescriptorIsPresentedAsNotFound() {
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        DescriptorValidatorPresenter presenter = mock(DescriptorValidatorPresenter.class, CALLS_REAL_METHODS);
        when(parserOutboundPort.descriptorExists()).thenReturn(false);

        assertThatThrownBy(() -> newValidator("nope.json", presenter, odmOutboundPort, parserOutboundPort).execute())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Descriptor root path does not exist: nope.json");
        verify(parserOutboundPort, never()).getCanonicalRawDescriptor();
        verify(odmOutboundPort, never()).validateDataProduct(any(), any());
        verify(presenter, never()).presentValidationResults(any());
    }

    @Test
    public void testBlankDescriptorPathIsPresentedAsInvalid() {
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);

        assertThatThrownBy(() -> newValidator("  ", results -> { }, odmOutboundPort, parserOutboundPort).execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Descriptor root path must not be empty.");
        verifyNoInteractions(parserOutboundPort, odmOutboundPort);
    }

    @Test
    public void testDirectoryDescriptorPathIsPresentedAsInvalid() {
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        when(parserOutboundPort.descriptorExists()).thenReturn(true);
        when(parserOutboundPort.isDescriptorDirectory()).thenReturn(true);

        assertThatThrownBy(() -> newValidator("descriptors", results -> { }, odmOutboundPort, parserOutboundPort).execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Descriptor root path must be a file, not a directory: descriptors");
        verify(odmOutboundPort, never()).validateDataProduct(any(), any());
    }

    @Test
    public void testEmptyDescriptorIsPresentedAsInvalid() {
        DescriptorValidatorParserOutboundPort parserOutboundPort = mock(DescriptorValidatorParserOutboundPort.class);
        DescriptorValidatorOdmOutboundPort odmOutboundPort = mock(DescriptorValidatorOdmOutboundPort.class);
        when(parserOutboundPort.descriptorExists()).thenReturn(true);
        when(parserOutboundPort.isDescriptorEmpty()).thenReturn(true);

        assertThatThrownBy(() -> newValidator("empty.json", results -> { }, odmOutboundPort, parserOutboundPort).execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Descriptor file is empty: empty.json");
        verify(odmOutboundPort, never()).validateDataProduct(any(), any());
    }

    private DescriptorValidator newValidator(String descriptorFilePath, DescriptorValidatorPresenter presenter, DescriptorValidatorOdmOutboundPort odmOutboundPort, DescriptorValidatorParserOutboundPort parserOutboundPort) {
        return new DescriptorValidator(new DescriptorValidatorCommand(descriptorFilePath, null), presenter, odmOutboundPort, parserOutboundPort);
    }
}

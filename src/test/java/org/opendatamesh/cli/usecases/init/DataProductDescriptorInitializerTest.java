package org.opendatamesh.cli.usecases.init;

import org.junit.jupiter.api.Test;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.info.Info;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataProductDescriptorInitializerTest {

    @Test
    public void testInitDescriptorWithAllOptions() {
        DataProductDescriptorInitializerParserOutboundPort parserOutboundPort = mock(DataProductDescriptorInitializerParserOutboundPort.class);
        DataProductDescriptorInitializerCommand command = new DataProductDescriptorInitializerCommand(
                "domain", "name", "Display Name", "2.0.0", "urn:dp:domain:name",
                "a description", "owner@test.com", "Owner", "out.json", true
        );
        AtomicReference<DataProductVersion> presented = new AtomicReference<>();

        new DataProductDescriptorInitializer(command, presented::set, parserOutboundPort).execute();

        Info info = presented.get().getInfo();
        assertThat(info.getDomain()).isEqualTo("domain");
        assertThat(info.getName()).isEqualTo("name");
        assertThat(info.getDisplayName()).isEqualTo("Display Name");
        assertThat(info.getVersion()).isEqualTo("2.0.0");
        assertThat(info.getFullyQualifiedName()).isEqualTo("urn:dp:domain:name");
        assertThat(info.getDescription()).isEqualTo("a description");
        assertThat(info.getOwner().getId()).isEqualTo("owner@test.com");
        assertThat(info.getOwner().getName()).isEqualTo("Owner");
        verify(parserOutboundPort).saveObject(presented.get(), "out.json", true);
    }

    @Test
    public void testInitDescriptorAppliesDefaults() {
        DataProductDescriptorInitializerParserOutboundPort parserOutboundPort = mock(DataProductDescriptorInitializerParserOutboundPort.class);
        DataProductDescriptorInitializerCommand command = new DataProductDescriptorInitializerCommand(
                "domain", "name", null, "1.0.0", null,
                null, null, null, "out.yaml", false
        );
        AtomicReference<DataProductVersion> presented = new AtomicReference<>();

        new DataProductDescriptorInitializer(command, presented::set, parserOutboundPort).execute();

        Info info = presented.get().getInfo();
        assertThat(info.getDisplayName()).isEqualTo("name");
        assertThat(info.getFullyQualifiedName()).isEqualTo("domain:name:null");
        assertThat(info.getOwner().getId()).isEqualTo("{defaultOwnerId}");
        assertThat(info.getOwner().getName()).isEqualTo("{defaultOwnerName}");
        assertThat(presented.get().getInterfaceComponents()).isNotNull();
        verify(parserOutboundPort).saveObject(presented.get(), "out.yaml", false);
    }
}

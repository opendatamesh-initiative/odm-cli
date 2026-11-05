package org.opendatamesh.cli.usecases.devops.starttask;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.cli.clients.platform.registry.PlatformRegistryClient;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests of the lazy Registry client. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class StartTaskRegistryOutboundPortImplTest {

    @Test
    void whenNoCallIsMadeThenNoClientIsCreated() {
        AtomicInteger created = new AtomicInteger();

        new StartTaskRegistryOutboundPortImpl(() -> {
            created.incrementAndGet();
            return Mockito.mock(PlatformRegistryClient.class);
        });

        assertThat(created).hasValue(0);
    }

    @Test
    void whenSeveralCallsAreMadeThenTheClientIsCreatedOnce() {
        AtomicInteger created = new AtomicInteger();
        StartTaskRegistryOutboundPortImpl port = new StartTaskRegistryOutboundPortImpl(() -> {
            created.incrementAndGet();
            return Mockito.mock(PlatformRegistryClient.class);
        });

        port.findDataProductsByFqn("urn:dp:sales");
        port.findDataProductVersions("p1", "1.2.0");
        port.getDataProductVersion("v1");

        assertThat(created).hasValue(1);
    }
}

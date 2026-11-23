package org.opendatamesh.cli.usecases.devops.starttask;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.PlatformDevOpsClient;
import org.opendatamesh.cli.exceptions.BadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opendatamesh.cli.usecases.devops.starttask.StartTaskFakes.activity;

/** Tests of the race detection on Execute Activity. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class StartTaskDevOpsOutboundPortImplTest {

    private final PlatformDevOpsClient client = Mockito.mock(PlatformDevOpsClient.class);
    private final StartTaskDevOpsOutboundPortImpl port = new StartTaskDevOpsOutboundPortImpl(client);

    @Test
    void whenExecuteSucceedsThenActivityIsCreated() {
        DevOpsActivity created = activity("a1", org.opendatamesh.cli.clients.platform.devops.ExecutionStatus.PENDING);
        when(client.executeActivity(any())).thenReturn(created);

        assertThat(port.executeActivity(created)).isEqualTo(new ActivityCreation.Created(created));
    }

    @Test
    void whenDevOpsRefusesBecauseAnActivityIsAlreadyOpenThenReportsAlreadyOpen() {
        when(client.executeActivity(any())).thenThrow(new BadRequestException(
                "Activity dev of data product version 1.2.0 " + StartTaskDevOpsOutboundPortImpl.ALREADY_OPEN_MESSAGE));

        assertThat(port.executeActivity(activity("a1", null))).isEqualTo(new ActivityCreation.AlreadyOpen());
    }

    @Test
    void whenDevOpsRefusesForAnotherReasonThenTheRefusalPropagates() {
        when(client.executeActivity(any())).thenThrow(new BadRequestException("Activity name is required"));

        assertThatThrownBy(() -> port.executeActivity(activity("a1", null)))
                .isInstanceOf(BadRequestException.class).hasMessage("Activity name is required");
    }

    /** Pins the wording emitted by {@code ExecuteActivity.java:203} of odm-platform-pp-devops-server. */
    @Test
    void theRaceMessageIsTheOneDevOpsSends() {
        assertThat(StartTaskDevOpsOutboundPortImpl.ALREADY_OPEN_MESSAGE).isEqualTo("is already PENDING or RUNNING");
    }
}

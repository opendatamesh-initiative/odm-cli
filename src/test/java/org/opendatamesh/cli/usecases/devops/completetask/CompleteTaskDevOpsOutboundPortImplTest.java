package org.opendatamesh.cli.usecases.devops.completetask;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.clients.platform.devops.PlatformDevOpsClient;
import org.opendatamesh.cli.exceptions.BadRequestException;
import org.opendatamesh.cli.exceptions.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/** Tests of the run-id writes. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class CompleteTaskDevOpsOutboundPortImplTest {

    private final PlatformDevOpsClient client = Mockito.mock(PlatformDevOpsClient.class);
    private final CompleteTaskDevOpsOutboundPortImpl port = new CompleteTaskDevOpsOutboundPortImpl(client);

    @Test
    void whenDevOpsAcceptsThenWritesAreRecorded() {
        assertThat(port.recordLogs("run-1", List.of("a"))).isEqualTo(TaskWrite.RECORDED);
        assertThat(port.recordResults("run-1", "{}")).isEqualTo(TaskWrite.RECORDED);
        assertThat(port.recordStatus("run-1", ExecutionStatus.SUCCEEDED)).isEqualTo(TaskWrite.RECORDED);
        verify(client).recordTaskLogsByRunId("run-1", List.of("a"));
        verify(client).recordTaskResultsByRunId("run-1", "{}");
        verify(client).recordTaskStatusByRunId("run-1", ExecutionStatus.SUCCEEDED);
    }

    @Test
    void whenDevOpsHasNoRunningTaskThenWriteReportsNoRunningTask() {
        doThrow(new NotFoundException("No RUNNING task with provider run id run-1"))
                .when(client).recordTaskResultsByRunId("run-1", "{}");

        assertThat(port.recordResults("run-1", "{}")).isEqualTo(TaskWrite.NO_RUNNING_TASK);
    }

    @Test
    void whenDevOpsRefusesThenTheRefusalPropagates() {
        doThrow(new BadRequestException("Task build is not instrumented"))
                .when(client).recordTaskStatusByRunId("run-1", ExecutionStatus.FAILED);

        assertThatThrownBy(() -> port.recordStatus("run-1", ExecutionStatus.FAILED))
                .isInstanceOf(BadRequestException.class).hasMessage("Task build is not instrumented");
    }
}

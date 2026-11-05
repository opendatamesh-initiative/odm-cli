package org.opendatamesh.cli.usecases.devops.completetask;

import java.time.Duration;

interface CompleteTaskClockOutboundPort {
    void sleep(Duration duration);
}

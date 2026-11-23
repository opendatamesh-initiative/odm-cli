package org.opendatamesh.cli.usecases.devops.starttask;

import java.time.Duration;
import java.time.Instant;

interface StartTaskClockOutboundPort {
    Instant now();

    void sleep(Duration duration);
}

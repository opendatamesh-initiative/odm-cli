package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.exceptions.InternalException;

import java.time.Duration;
import java.time.Instant;

class StartTaskClockOutboundPortImpl implements StartTaskClockOutboundPort {

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting", e);
        }
    }
}

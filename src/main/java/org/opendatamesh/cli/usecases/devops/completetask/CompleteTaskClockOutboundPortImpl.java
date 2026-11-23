package org.opendatamesh.cli.usecases.devops.completetask;

import org.opendatamesh.cli.exceptions.InternalException;

import java.time.Duration;

class CompleteTaskClockOutboundPortImpl implements CompleteTaskClockOutboundPort {

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

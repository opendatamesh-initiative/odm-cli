package org.opendatamesh.cli.commands.platform.devops;

import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import picocli.CommandLine;

import java.util.Locale;

/** Accepts SUCCEEDED, FAILED or CANCELED, in any case. */
public class TaskStatusConverter implements CommandLine.ITypeConverter<ExecutionStatus> {

    @Override
    public ExecutionStatus convert(String value) {
        ExecutionStatus status = parse(value);
        if (status == null || status == ExecutionStatus.PENDING || status == ExecutionStatus.RUNNING) {
            throw new CommandLine.TypeConversionException("value '" + value + "' is not a final status: expected SUCCEEDED, FAILED or CANCELED");
        }
        return status;
    }

    private ExecutionStatus parse(String value) {
        try {
            return ExecutionStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}

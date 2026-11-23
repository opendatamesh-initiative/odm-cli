package org.opendatamesh.cli.commands.platform.devops;

import org.springframework.boot.convert.DurationStyle;
import picocli.CommandLine;

import java.time.Duration;

/** Accepts a duration such as 30s, 10m or PT10M that is positive and at least the given minimum. */
public class DurationConverter implements CommandLine.ITypeConverter<Duration> {

    private final Duration minimum;

    public DurationConverter() {
        this(Duration.ZERO);
    }

    public DurationConverter(Duration minimum) {
        this.minimum = minimum;
    }

    @Override
    public Duration convert(String value) {
        Duration duration = parse(value);
        if (duration.isZero() || duration.isNegative()) {
            throw new CommandLine.TypeConversionException("value '" + value + "' must be a positive duration, for example 30s, 10m or PT10M");
        }
        if (duration.compareTo(minimum) < 0) {
            throw new CommandLine.TypeConversionException("value '" + value + "' must be at least " + minimum);
        }
        return duration;
    }

    private Duration parse(String value) {
        try {
            return DurationStyle.detectAndParse(value.trim());
        } catch (RuntimeException e) {
            throw new CommandLine.TypeConversionException("value '" + value + "' is not a duration, for example 30s, 10m or PT10M");
        }
    }
}

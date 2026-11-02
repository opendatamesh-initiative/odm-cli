package org.opendatamesh.cli.commands.platform.devops;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import picocli.CommandLine;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests of the usage-level converters. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class DevOpsConvertersTest {

    private final ResolvedValueConverter resolved = new ResolvedValueConverter();

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "${CI_PIPELINE_ID}", "run-${ID}", "$CI_PIPELINE_ID", "$(git rev-parse HEAD)"})
    void whenValueIsEmptyOrAnUnresolvedReferenceThenItIsRejected(String value) {
        assertThatThrownBy(() -> resolved.convert(value)).isInstanceOf(CommandLine.TypeConversionException.class)
                .hasMessageContaining("is empty or an unresolved pipeline variable");
    }

    @ParameterizedTest
    @ValueSource(strings = {"4242", "run-1", "urn:dp:sales", "price$ ", "a$b"})
    void whenValueIsResolvedThenItIsAccepted(String value) {
        assertThat(resolved.convert(value)).isEqualTo(value);
    }

    @Test
    void whenStatusIsFinalThenItIsAcceptedInAnyCase() {
        TaskStatusConverter converter = new TaskStatusConverter();

        assertThat(converter.convert("succeeded")).isEqualTo(ExecutionStatus.SUCCEEDED);
        assertThat(converter.convert("FAILED")).isEqualTo(ExecutionStatus.FAILED);
        assertThat(converter.convert("Canceled")).isEqualTo(ExecutionStatus.CANCELED);
        assertThatThrownBy(() -> converter.convert("RUNNING")).isInstanceOf(CommandLine.TypeConversionException.class);
        assertThatThrownBy(() -> converter.convert("done")).isInstanceOf(CommandLine.TypeConversionException.class);
    }

    @Test
    void whenDurationIsPositiveThenItIsParsed() {
        DurationConverter converter = new DurationConverter();

        assertThat(converter.convert("30s")).isEqualTo(Duration.ofSeconds(30));
        assertThat(converter.convert("10m")).isEqualTo(Duration.ofMinutes(10));
        assertThat(converter.convert("PT10M")).isEqualTo(Duration.ofMinutes(10));
        assertThatThrownBy(() -> converter.convert("0s")).isInstanceOf(CommandLine.TypeConversionException.class);
        assertThatThrownBy(() -> converter.convert("-5s")).isInstanceOf(CommandLine.TypeConversionException.class);
        assertThatThrownBy(() -> converter.convert("soon")).isInstanceOf(CommandLine.TypeConversionException.class);
    }

    @Test
    void whenDurationHasAMinimumThenShorterValuesAreRejected() {
        DurationConverter converter = new DurationConverter(Duration.ofSeconds(1));

        assertThat(converter.convert("1s")).isEqualTo(Duration.ofSeconds(1));
        assertThat(converter.convert("5s")).isEqualTo(Duration.ofSeconds(5));
        assertThatThrownBy(() -> converter.convert("10ms")).isInstanceOf(CommandLine.TypeConversionException.class)
                .hasMessageContaining("must be at least PT1S");
    }
}

package org.opendatamesh.cli.commands.platform.devops;

import picocli.CommandLine;

import java.util.regex.Pattern;

/**
 * Rejects a blank value, and a value that is still an unresolved CI reference ({@code ${...}}, {@code $NAME}
 * or {@code $(...)}), so the pipeline gets a usage error (exit 2) instead of acting on a literal.
 */
public class ResolvedValueConverter implements CommandLine.ITypeConverter<String> {

    private static final Pattern BRACED_REFERENCE = Pattern.compile(".*\\$\\{.*", Pattern.DOTALL);
    private static final Pattern NAMED_REFERENCE = Pattern.compile("^\\$[A-Za-z_][A-Za-z0-9_]*$");
    private static final Pattern COMMAND_REFERENCE = Pattern.compile("^\\$\\(.*\\)$", Pattern.DOTALL);

    @Override
    public String convert(String value) {
        if (value == null || value.isBlank() || unresolved(value)) {
            throw new CommandLine.TypeConversionException("value '" + value + "' is empty or an unresolved pipeline variable");
        }
        return value;
    }

    private boolean unresolved(String value) {
        return BRACED_REFERENCE.matcher(value).matches()
                || NAMED_REFERENCE.matcher(value).matches()
                || COMMAND_REFERENCE.matcher(value).matches();
    }
}

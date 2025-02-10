package org.opendatamesh.cli.utils;

import org.opendatamesh.cli.extensions.ExtensionOption;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

public abstract class CommandOptionsUtils {

    private CommandOptionsUtils() {
    }

    public static Optional<String> getOptionFromArguments(String[] args, String option) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith(option)) {
                if (arg.contains("=")) {
                    return Optional.of(arg.substring(arg.indexOf('=') + 1));
                }
                if (i + 1 < args.length) {
                    return Optional.ofNullable(args[i + 1]);
                }
            }
        }
        return Optional.empty();
    }

    public static void handleRequiredOptions(List<ExtensionOption> extensionOptions) {
        for (ExtensionOption extensionOption : extensionOptions) {
            if (extensionOption.isRequired() &&
                    !StringUtils.hasText(extensionOption.getGetter().get())
            ) {
                String extensionName = extensionOption.getNames().stream().findFirst().orElse("").replace("-", "");
                if (extensionOption.isInteractive()) {
                    String value = System.console().readLine(
                            String.format("Enter value for %s (%s): ",
                                    extensionName,
                                    extensionOption.getDescription()
                            )
                    );
                    extensionOption.getSetter().accept(value);
                } else {
                    throw new IllegalStateException("Missing value for parameter:" + extensionName);
                }
            }
        }
    }
}

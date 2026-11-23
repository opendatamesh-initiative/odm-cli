package org.opendatamesh.cli.usecases.devops;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * README guard of the module {@code usecases/devops} (PROGRESSIVE_DISCOVERY). Part of BDMD-5443, see the
 * prompt in {@code spdd/prompt/}. It reads {@code src/main/java}, so it starts no application.
 */
class DevOpsUseCasesReadmeGuardTest {

    private static final Path SOURCES = Path.of("src/main/java/org/opendatamesh/cli");
    private static final Path MODULE = SOURCES.resolve("usecases/devops");
    private static final List<String> UNITS = List.of("starttask", "completetask");
    private static final List<Path> CLIENT_DOMAIN_TYPES = List.of(
            SOURCES.resolve("clients/platform/devops"), SOURCES.resolve("clients/platform/registry"));
    private static final Path TESTS = Path.of("src/test/java/org/opendatamesh/cli");
    private static final Path COMMAND_TESTS = TESTS.resolve("commands/platform/devops");
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]+)`");
    private static final Pattern PASCAL_CASE = Pattern.compile("^[A-Z][a-z0-9]+([A-Z][A-Za-z0-9]*)+$");

    @Test
    void everyUnitReadmeHasAtMost80LinesAndNoCodeBlocks() throws IOException {
        for (String unit : UNITS) {
            List<String> lines = Files.readAllLines(MODULE.resolve(unit).resolve("README.md"));
            assertThat(lines.size()).as("%s README lines", unit).isLessThanOrEqualTo(80);
            assertThat(lines).as("%s README code fences", unit).noneMatch(line -> line.trim().startsWith("```"));
        }
    }

    @Test
    void everyPascalCaseNameInInlineCodeIsATypeOfTheUnitOrOfTheClientModel() throws IOException {
        for (String unit : UNITS) {
            Set<String> knownTypes = typesDeclaredIn(MODULE.resolve(unit));
            // The README ends with the tests that prove the unit: its own tests and the command integration test.
            knownTypes.addAll(typesDeclaredIn(TESTS.resolve("usecases/devops").resolve(unit)));
            knownTypes.addAll(typesDeclaredIn(COMMAND_TESTS));
            CLIENT_DOMAIN_TYPES.forEach(directory -> knownTypes.addAll(typesDeclaredIn(directory)));
            String readme = Files.readString(MODULE.resolve(unit).resolve("README.md"));
            Matcher inlineCode = INLINE_CODE.matcher(readme);
            while (inlineCode.find()) {
                String token = inlineCode.group(1);
                if (PASCAL_CASE.matcher(token).matches()) {
                    assertThat(knownTypes).as("%s README names %s", unit, token).contains(token);
                }
            }
        }
    }

    @Test
    void theIndexLinksEveryUnitReadme() throws IOException {
        String index = Files.readString(MODULE.resolve("README.md"));
        for (String unit : UNITS) {
            assertThat(index).contains("](" + unit + "/README.md)");
        }
    }

    @Test
    void theIndexNamesOnlyExistingUnits() throws IOException {
        try (Stream<Path> directories = Files.list(MODULE)) {
            Set<String> units = new HashSet<>();
            directories.filter(Files::isDirectory).forEach(directory -> units.add(directory.getFileName().toString()));
            assertThat(units).containsExactlyInAnyOrderElementsOf(UNITS);
        }
    }

    private static Set<String> typesDeclaredIn(Path directory) {
        Set<String> types = new HashSet<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .forEach(name -> types.add(name.substring(0, name.length() - ".java".length())));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return types;
    }
}

package com.varun.taskforge;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Guards the feature boundaries described in docs/adr/0001-package-by-feature-modular-monolith.md
 * by scanning import statements in the main source tree. No extra dependency is needed.
 */
class ModuleBoundaryTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java/com/varun/taskforge");
    private static final String BASE = "com.varun.taskforge.";

    @ParameterizedTest(name = "{0} must not depend on {1}")
    @CsvSource({
            "system, task",
            "task,   system",
            "system, shared",
            "task,   shared",
    })
    void featureDoesNotImportForbiddenModule(String module, String forbidden) throws IOException {
        assertThat(importsOf(module, BASE + forbidden + "."))
                .as("imports of %s from %s", forbidden, module)
                .isEmpty();
    }

    private static List<String> importsOf(String module, String forbiddenPrefix) throws IOException {
        Path moduleDir = MAIN_SOURCES.resolve(module);
        assertThat(moduleDir).isDirectory();
        try (Stream<Path> files = Files.walk(moduleDir)) {
            return files.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(ModuleBoundaryTest::lines)
                    .map(String::trim)
                    .filter(line -> line.startsWith("import " + forbiddenPrefix)
                            || line.startsWith("import static " + forbiddenPrefix))
                    .toList();
        }
    }

    private static Stream<String> lines(Path file) {
        try {
            return Files.readAllLines(file).stream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
    }
}

package io.github.sibmaks.jjtemplate.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author sibmaks
 */
class TemplateCompilerIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TemplateCompiler compiler = new TemplateCompiler();

    private static Arguments buildArguments(Path it) {
        try {
            var templateScript = OBJECT_MAPPER.readValue(it.resolve("input.json").toFile(), TemplateScript.class);
            var contextPath = it.resolve("variables.json").toFile();
            Map<String, Object> context = Map.of();
            if (contextPath.exists()) {
                context = OBJECT_MAPPER.readValue(contextPath, new TypeReference<>() {
                });
            }
            var excepted = OBJECT_MAPPER.readValue(it.resolve("excepted.json").toFile(), Object.class);
            return Arguments.of(
                    templateScript,
                    context,
                    excepted
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> getCases(Path resourcesDir) {
        try (var paths = Files.list(resourcesDir)) {
            return paths
                    .filter(Files::isDirectory)
                    .peek(it -> System.out.printf("Directory: %s%n", it))
                    .map(Path::toAbsolutePath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    void testScenario(TemplateScript templateScript, Map<String, Object> context, Object excepted) {
        var compiled = compiler.compile(templateScript);
        assertNotNull(compiled);
        var rendered = compiled.render(context);
        assertEquals(excepted, rendered);
    }

    public static Stream<Arguments> cases() {
        var resourcesDir = Paths.get("src", "test", "resources", "cases");

        var cases = getCases(resourcesDir);

        return cases
                .stream()
                .map(TemplateCompilerIntegrationTest::buildArguments);
    }
}
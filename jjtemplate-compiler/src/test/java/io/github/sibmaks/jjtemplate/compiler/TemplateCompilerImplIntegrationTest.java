package io.github.sibmaks.jjtemplate.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sibmaks.jjtemplate.compiler.api.Definition;
import io.github.sibmaks.jjtemplate.compiler.api.MapTemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.compiler.impl.StaticCompiledTemplateImpl;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@Timeout(
        value = 5,
        unit = TimeUnit.SECONDS,
        threadMode = Timeout.ThreadMode.SEPARATE_THREAD
)
class TemplateCompilerImplIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    private static Arguments buildArguments(Path root, Path it) {
        try {
            var templateScript = OBJECT_MAPPER.readValue(it.resolve("input.jjt").toFile(), TemplateScript.class);
            var contextPath = it.resolve("variables.json").toFile();
            var context = Map.<String, Object>of();
            if (contextPath.exists()) {
                context = OBJECT_MAPPER.readValue(contextPath, new TypeReference<>() {
                });
            }
            var expected = OBJECT_MAPPER.readValue(it.resolve("expected.json").toFile(), Object.class);
            var path = root.toAbsolutePath().normalize().toString();
            return Arguments.of(
                    it.toAbsolutePath().normalize().toString().substring(path.length() + 1),
                    templateScript,
                    context,
                    expected
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Arguments buildErrorArguments(Path root, Path it) {
        try {
            var templateScript = OBJECT_MAPPER.readValue(it.resolve("input.jjt").toFile(), TemplateScript.class);
            var contextPath = it.resolve("variables.json").toFile();
            var context = Map.<String, Object>of();
            if (contextPath.exists()) {
                context = OBJECT_MAPPER.readValue(contextPath, new TypeReference<>() {
                });
            }
            var expectation = OBJECT_MAPPER.readValue(
                    it.resolve("expected-error.json").toFile(),
                    new TypeReference<Map<String, String>>() {
                    }
            );
            var path = root.toAbsolutePath().normalize().toString();
            return Arguments.of(
                    it.toAbsolutePath().normalize().toString().substring(path.length() + 1),
                    templateScript,
                    context,
                    expectation.get("phase"),
                    expectation.get("exception"),
                    expectation.get("messageContains")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> getCases(Path resourcesDir) {
        try (var paths = Files.walk(resourcesDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(it -> "input.jjt".equals(it.getFileName().toString()))
                    .map(Path::getParent)
                    .map(Path::toAbsolutePath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isLoadEnabled() {
        var property = System.getProperty("io.github.sibmaks.jjtemplate.compiler.loadEnabled");
        return Boolean.parseBoolean(property);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getClassOf(T[] array) {
        return (Class<T>) array.getClass().getComponentType();
    }

    @ParameterizedTest
    @MethodSource("cases")
    void testScenario(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object expected
    ) {
        var compiler = TemplateCompiler.getInstance();
        var begin = System.nanoTime();
        var compiled = compiler.compile(templateScript);
        var compiledAt = System.nanoTime();
        assertNotNull(compiled);
        var rendered = compiled.render(context);
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(expected, renderedJson);
        System.out.printf(
                "Case '%s', compiled: %.4f ms, rendered: %.4f ms%n",
                caseName,
                (compiledAt - begin) / 1000000.0,
                (renderedAt - compiledAt) / 1000000.0
        );
    }

    @ParameterizedTest
    @MethodSource("errorCases")
    void testErrorScenario(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            String phase,
            String exceptionType,
            String messageContains
    ) {
        var compiler = TemplateCompiler.getInstance();
        Executable action;
        if ("compile".equals(phase)) {
            action = () -> compiler.compile(templateScript);
        } else if ("render".equals(phase)) {
            var compiled = compiler.compile(templateScript);
            action = () -> compiled.render(context);
        } else {
            fail("Unsupported error phase '" + phase + "' for case " + caseName);
            return;
        }

        var exception = assertThrows(RuntimeException.class, action);

        assertEquals(exceptionType, exception.getClass().getName());
        assertTrue(
                hasMessageInCauseChain(exception, messageContains),
                () -> "Expected cause chain containing '" + messageContains + "', got: " + describeCauseChain(exception)
        );
    }

    private static String describeCauseChain(Throwable exception) {
        var description = new StringBuilder();
        var current = exception;
        while (current != null) {
            if (description.length() > 0) {
                description.append(" -> ");
            }
            description.append(current.getClass().getName())
                    .append(": ")
                    .append(current.getMessage());
            current = current.getCause();
        }
        return description.toString();
    }

    private static boolean hasMessageInCauseChain(Throwable exception, String expectedMessage) {
        var current = exception;
        while (current != null) {
            var message = current.getMessage();
            if (message != null && message.contains(expectedMessage)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @ParameterizedTest
    @MethodSource("cases")
    void testScenarioWithTypedContextFromVariables(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object expected
    ) {
        var compiler = TemplateCompiler.getInstance();
        var compileContext = new MapTemplateCompileContext(buildTypesFromContext(context));
        var begin = System.nanoTime();
        var compiled = compiler.compile(templateScript, compileContext);
        var compiledAt = System.nanoTime();
        assertNotNull(compiled);
        var rendered = compiled.render(context);
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(expected, renderedJson);
        System.out.printf(
                "Case '%s', compiled with typed context: %.4f ms, rendered: %.4f ms%n",
                caseName,
                (compiledAt - begin) / 1000000.0,
                (renderedAt - compiledAt) / 1000000.0
        );
    }

    @ParameterizedTest
    @MethodSource("staticCompilationCases")
    void testStaticCompilationScenario(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object expected
    ) {
        var compiler = TemplateCompiler.getInstance();
        var begin = System.nanoTime();
        var compiled = compiler.compile(templateScript);
        var compiledAt = System.nanoTime();
        var rendered = compiled.render(context);
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(expected, renderedJson);
        assertInstanceOf(StaticCompiledTemplateImpl.class, compiled);
        System.out.printf(
                "Case '%s', compiled: %.4f ms, rendered: %.4f ms%n",
                caseName,
                (compiledAt - begin) / 1000000.0,
                (renderedAt - compiledAt) / 1000000.0
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void testScenarioWithoutOptimization(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object expected
    ) {
        var options = TemplateCompileOptions.builder()
                .optimize(false)
                .build();
        var compiler = TemplateCompiler.getInstance(options);
        var begin = System.nanoTime();
        var compiled = compiler.compile(templateScript);
        var compiledAt = System.nanoTime();
        assertNotNull(compiled);
        var rendered = compiled.render(context);
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(expected, renderedJson);
        System.out.printf(
                "Case '%s', compiled: %.4f ms, rendered: %.4f ms%n",
                caseName,
                (compiledAt - begin) / 1000000.0,
                (renderedAt - compiledAt) / 1000000.0
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void testScenarioWithArrays(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object expected
    ) {
        var compiler = TemplateCompiler.getInstance();
        var modifiedDefinitions = new ArrayList<Definition>();
        for (var definition : Optional.ofNullable(templateScript.getDefinitions()).orElseGet(Collections::emptyList)) {
            var modifiedDefinition = new Definition();
            for (var entry : definition.entrySet()) {
                modifiedDefinition.put(entry.getKey(), listsToArrays(entry.getValue()));
            }
            modifiedDefinitions.add(modifiedDefinition);
        }
        var modifiedTemplateScript = TemplateScript.builder()
                .template(listsToArrays(templateScript.getTemplate()))
                .definitions(modifiedDefinitions)
                .build();
        var begin = System.nanoTime();
        var compiled = compiler.compile(modifiedTemplateScript);
        var compiledAt = System.nanoTime();
        assertNotNull(compiled);
        var rendered = compiled.render(listsToArrays(context));
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(expected, renderedJson);
        System.out.printf(
                "Case '%s', compiled: %.4f ms, rendered: %.4f ms%n",
                caseName,
                (compiledAt - begin) / 1000000.0,
                (renderedAt - compiledAt) / 1000000.0
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void testScenarioWithArraysAndWithoutOptimization(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object expected
    ) {
        var options = TemplateCompileOptions.builder()
                .optimize(false)
                .build();
        var compiler = TemplateCompiler.getInstance(options);
        var modifiedDefinitions = new ArrayList<Definition>();
        for (var definition : Optional.ofNullable(templateScript.getDefinitions()).orElseGet(Collections::emptyList)) {
            var modifiedDefinition = new Definition();
            for (var entry : definition.entrySet()) {
                modifiedDefinition.put(entry.getKey(), listsToArrays(entry.getValue()));
            }
            modifiedDefinitions.add(modifiedDefinition);
        }
        var modifiedTemplateScript = TemplateScript.builder()
                .template(listsToArrays(templateScript.getTemplate()))
                .definitions(modifiedDefinitions)
                .build();
        var begin = System.nanoTime();
        var compiled = compiler.compile(modifiedTemplateScript);
        var compiledAt = System.nanoTime();
        assertNotNull(compiled);
        var rendered = compiled.render(listsToArrays(context));
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(expected, renderedJson);
        System.out.printf(
                "Case '%s', compiled: %.4f ms, rendered: %.4f ms%n",
                caseName,
                (compiledAt - begin) / 1000000.0,
                (renderedAt - compiledAt) / 1000000.0
        );
    }

    @EnabledIf("isLoadEnabled")
    @ParameterizedTest
    @MethodSource("cases")
    void testScenarioWithLoad(String caseName,
                              TemplateScript templateScript,
                              Map<String, Object> context,
                              Object expected) {
        var compiler = TemplateCompiler.getInstance();
        var measurementsAmount = 10_000;
        var measurementsCompiled = new double[measurementsAmount];
        var measurementsRendered = new double[measurementsAmount];
        for (int i = 0; i < measurementsAmount; i++) {
            var begin = System.nanoTime();
            var compiled = compiler.compile(templateScript);
            var compiledAt = System.nanoTime();
            assertNotNull(compiled);
            var rendered = compiled.render(context);
            var renderedAt = System.nanoTime();
            var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
            assertEquals(expected, renderedJson);
            measurementsCompiled[i] = (compiledAt - begin) / 1000000.0;
            measurementsRendered[i] = (renderedAt - compiledAt) / 1000000.0;
        }
        var compileStats = Arrays.stream(measurementsCompiled).summaryStatistics();
        var renderStats = Arrays.stream(measurementsRendered).summaryStatistics();
        System.out.printf(
                "Case '%s', compiled: %.4f ms (%.4f - %.4f), rendered: %.4f ms (%.4f - %.4f), took: %.4f ms%n",
                caseName,
                compileStats.getAverage(),
                compileStats.getMin(),
                compileStats.getMax(),
                renderStats.getAverage(),
                renderStats.getMin(),
                renderStats.getMax(),
                compileStats.getSum() + renderStats.getSum()
        );
    }

    @EnabledIf("isLoadEnabled")
    @ParameterizedTest
    @MethodSource("cases")
    void testScenarioWithLoadWithoutOptimization(String caseName,
                                                 TemplateScript templateScript,
                                                 Map<String, Object> context,
                                                 Object expected) {
        var options = TemplateCompileOptions.builder()
                .optimize(false)
                .build();
        var compiler = TemplateCompiler.getInstance(options);
        var measurementsAmount = 10_000;
        var measurementsCompiled = new double[measurementsAmount];
        var measurementsRendered = new double[measurementsAmount];
        for (int i = 0; i < measurementsAmount; i++) {
            var begin = System.nanoTime();
            var compiled = compiler.compile(templateScript);
            var compiledAt = System.nanoTime();
            assertNotNull(compiled);
            var rendered = compiled.render(context);
            var renderedAt = System.nanoTime();
            var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
            assertEquals(expected, renderedJson);
            measurementsCompiled[i] = (compiledAt - begin) / 1000000.0;
            measurementsRendered[i] = (renderedAt - compiledAt) / 1000000.0;
        }
        var compileStats = Arrays.stream(measurementsCompiled).summaryStatistics();
        var renderStats = Arrays.stream(measurementsRendered).summaryStatistics();
        System.out.printf(
                "Case '%s', compiled: %.4f ms (%.4f - %.4f), rendered: %.4f ms (%.4f - %.4f), took: %.4f ms%n",
                caseName,
                compileStats.getAverage(),
                compileStats.getMin(),
                compileStats.getMax(),
                renderStats.getAverage(),
                renderStats.getMin(),
                renderStats.getMax(),
                compileStats.getSum() + renderStats.getSum()
        );
    }

    public static Stream<Arguments> cases() {
        var resourcesDir = Paths.get("src", "test", "resources", "cases");

        var cases = getCases(resourcesDir);

        return cases
                .stream()
                .filter(it -> Files.isRegularFile(it.resolve("expected.json")))
                .sorted()
                .map(it -> buildArguments(resourcesDir, it));
    }

    public static Stream<Arguments> errorCases() {
        var resourcesDir = Paths.get("src", "test", "resources", "cases");

        var cases = getCases(resourcesDir);

        return cases
                .stream()
                .filter(it -> Files.isRegularFile(it.resolve("expected-error.json")))
                .sorted()
                .map(it -> buildErrorArguments(resourcesDir, it));
    }

    public static Stream<Arguments> staticCompilationCases() {
        var resourcesDir = Paths.get(
                "src", "test", "resources", "cases", "optimization", "static-compilation"
        );

        var cases = getCases(resourcesDir);

        return cases
                .stream()
                .sorted()
                .map(it -> buildArguments(resourcesDir, it));
    }

    private static Map<String, List<Class<?>>> buildTypesFromContext(Map<String, Object> context) {
        var types = new LinkedHashMap<String, List<Class<?>>>(context.size());
        for (var entry : context.entrySet()) {
            var value = entry.getValue();
            if (value != null) {
                types.put(entry.getKey(), List.of(value.getClass()));
            }
        }
        return types;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T listsToArrays(Object value, T... reified) {
        var type = getClassOf(reified);

        if (value instanceof Map<?, ?>) {
            var map = (Map<String, Object>) value;
            var newMap = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                newMap.put(entry.getKey(), listsToArrays(entry.getValue()));
            }
            return type.cast(newMap);
        }

        if (value instanceof List<?>) {
            var list = (List<Object>) value;
            var arr = new Object[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = listsToArrays(list.get(i));
            }
            return type.cast(arr);
        }

        if (value != null && value.getClass().isArray()) {
            int len = Array.getLength(value);
            var arr = new Object[len];
            for (int i = 0; i < len; i++) {
                arr[i] = listsToArrays(Array.get(value, i));
            }
            return type.cast(arr);
        }

        return type.cast(value);
    }
}

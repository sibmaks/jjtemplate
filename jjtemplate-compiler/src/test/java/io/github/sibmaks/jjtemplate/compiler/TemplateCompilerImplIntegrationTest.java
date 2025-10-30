package io.github.sibmaks.jjtemplate.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sibmaks.jjtemplate.compiler.api.Definition;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author sibmaks
 */
class TemplateCompilerImplIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    private static Arguments buildArguments(Path it) {
        try {
            var templateScript = OBJECT_MAPPER.readValue(it.resolve("input.json").toFile(), TemplateScript.class);
            var contextPath = it.resolve("variables.json").toFile();
            var context = Map.<String, Object>of();
            if (contextPath.exists()) {
                context = OBJECT_MAPPER.readValue(contextPath, new TypeReference<>() {
                });
            }
            var excepted = OBJECT_MAPPER.readValue(it.resolve("excepted.json").toFile(), Object.class);
            return Arguments.of(
                    it.getFileName().toString(),
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

    @ParameterizedTest
    @MethodSource("cases")
    void testScenario(
            String caseName,
            TemplateScript templateScript,
            Map<String, Object> context,
            Object excepted
    ) {
        var compiler = TemplateCompiler.getInstance(Locale.US);
        var begin = System.nanoTime();
        var compiled = compiler.compile(templateScript);
        var compiledAt = System.nanoTime();
        assertNotNull(compiled);
        var rendered = compiled.render(context);
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(excepted, renderedJson);
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
            Object excepted
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
        assertEquals(excepted, renderedJson);
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
            Object excepted
    ) {
        var compiler = TemplateCompiler.getInstance(Locale.US);
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
        var rendered = compiled.render((Map<String, Object>) listsToArrays(context));
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(excepted, renderedJson);
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
            Object excepted
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
        var rendered = compiled.render((Map<String, Object>) listsToArrays(context));
        var renderedAt = System.nanoTime();
        var renderedJson = OBJECT_MAPPER.convertValue(rendered, Object.class);
        assertEquals(excepted, renderedJson);
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
                              Object excepted) {
        var compiler = TemplateCompiler.getInstance(Locale.US);
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
            assertEquals(excepted, renderedJson);
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
                                                 Object excepted) {
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
            assertEquals(excepted, renderedJson);
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
                .sorted()
                .map(TemplateCompilerImplIntegrationTest::buildArguments);
    }

    /**
     * Рекурсивно заменяет все List на массивы.
     * Работает с Map, List и примитивными значениями.
     */
    @SuppressWarnings("unchecked")
    public static Object listsToArrays(Object value) {
        if (value instanceof Map<?, ?>) {
            var map = (Map<String, Object>) value;
            Map<Object, Object> newMap = new LinkedHashMap<>();
            for (var entry : map.entrySet()) {
                newMap.put(entry.getKey(), listsToArrays(entry.getValue()));
            }
            return newMap;
        }

        if (value instanceof List<?>) {
            var list = (List<Object>) value;
            Object[] arr = new Object[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = listsToArrays(list.get(i));
            }
            return arr;
        }

        // Для массивов — применяем рекурсивно к элементам
        if (value != null && value.getClass().isArray()) {
            int len = Array.getLength(value);
            Object[] arr = new Object[len];
            for (int i = 0; i < len; i++) {
                arr[i] = listsToArrays(Array.get(value, i));
            }
            return arr;
        }

        // Примитивные значения возвращаем как есть
        return value;
    }
}
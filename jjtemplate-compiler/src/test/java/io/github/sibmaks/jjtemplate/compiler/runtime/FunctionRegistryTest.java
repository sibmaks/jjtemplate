package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class FunctionRegistryTest {

    @Test
    void shouldLoadBuiltInFunctionsWhenContextClassLoaderDoesNotSeeCompilerClasses() throws Exception {
        var previous = Thread.currentThread().getContextClassLoader();
        try (var isolated = new URLClassLoader(new URL[0], null)) {
            Thread.currentThread().setContextClassLoader(isolated);

            var registry = new FunctionRegistry(TemplateEvaluationOptions.builder().build());
            assertNotNull(registry.getFunction("cast", "str"));
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @Test
    void shouldReturnUserDefinedFunction() {
        var namespace = UUID.randomUUID().toString();
        var name = UUID.randomUUID().toString();

        TemplateFunction<String> function = mock();

        when(function.getNamespace())
                .thenReturn(namespace);
        when(function.getName())
                .thenReturn(name);

        var options = TemplateEvaluationOptions.builder()
                .functions(List.of(function))
                .build();

        var registry = new FunctionRegistry(options);

        var result = registry.getFunction(namespace, name);

        assertSame(function, result);
    }

    @Test
    void shouldThrowExceptionForUnknownNamespace() {
        var namespace = UUID.randomUUID().toString();
        var name = UUID.randomUUID().toString();

        var options = TemplateEvaluationOptions.builder()
                .functions(List.of())
                .build();

        var registry = new FunctionRegistry(options);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> registry.getFunction(namespace, name)
        );

        assertTrue(ex.getMessage().contains(namespace));
    }

    @Test
    void shouldThrowExceptionForUnknownFunctionInExistingNamespace() {
        var namespace = UUID.randomUUID().toString();
        var existingName = UUID.randomUUID().toString();
        var missingName = UUID.randomUUID().toString();

        TemplateFunction<String> function = mock();

        when(function.getNamespace())
                .thenReturn(namespace);
        when(function.getName())
                .thenReturn(existingName);

        var options = TemplateEvaluationOptions.builder()
                .functions(List.of(function))
                .build();

        var registry = new FunctionRegistry(options);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> registry.getFunction(namespace, missingName)
        );

        assertTrue(ex.getMessage().contains(missingName));
        assertTrue(ex.getMessage().contains(namespace));
    }

    @Test
    void shouldThrowExceptionForDuplicateUserFunctionNamesInSameNamespace() {
        var namespace = UUID.randomUUID().toString();
        var name = UUID.randomUUID().toString();

        TemplateFunction<String> function1 = mock("firstFunction");
        TemplateFunction<String> function2 = mock("secondFunction");

        when(function1.getNamespace()).thenReturn(namespace);
        when(function1.getName()).thenReturn(name);

        when(function2.getNamespace()).thenReturn(namespace);
        when(function2.getName()).thenReturn(name);

        var options = TemplateEvaluationOptions.builder()
                .functions(List.of(function1, function2))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> new FunctionRegistry(options)
        );
    }

    @Test
    void shouldAllowSameFunctionNameInDifferentNamespaces() {
        var name = UUID.randomUUID().toString();
        var namespace1 = UUID.randomUUID().toString();
        var namespace2 = UUID.randomUUID().toString();

        TemplateFunction<String> function1 = mock("firstFunction");
        TemplateFunction<String> function2 = mock("secondFunction");

        when(function1.getNamespace()).thenReturn(namespace1);
        when(function1.getName()).thenReturn(name);

        when(function2.getNamespace()).thenReturn(namespace2);
        when(function2.getName()).thenReturn(name);

        var options = TemplateEvaluationOptions.builder()
                .functions(List.of(function1, function2))
                .build();

        var registry = new FunctionRegistry(options);

        assertSame(function1, registry.getFunction(namespace1, name));
        assertSame(function2, registry.getFunction(namespace2, name));
    }
}

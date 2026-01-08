package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.map;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class MapEmptyTemplateFunctionTest {
    @InjectMocks
    private MapEmptyTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("map", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("empty", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void unsupportedType() {
        var args = List.<Object>of(true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("map:empty: unsupported type: " + Boolean.class, exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("map:empty: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("map:empty: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("map:empty: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("emptyCases")
    void empty(Object value, boolean expected) {
        var args = new ArrayList<>();
        args.add(value);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("emptyCases")
    void emptyViaPipe(Object value, boolean expected) {
        var actual = function.invoke(List.of(), value);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> emptyCases() {
        return Stream.of(
                Arguments.of(null, true),
                Arguments.of(Map.of(), true),
                Arguments.of(Map.of("key", "value"), false)
        );
    }

}
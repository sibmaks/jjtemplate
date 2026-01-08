package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.list;

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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ListTailTemplateFunctionTest {
    @InjectMocks
    private ListTailTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("list", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("tail", actual);
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
        assertEquals("list:tail: unsupported type: " + Boolean.class, exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:tail: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("list:tail: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:tail: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("tailCases")
    void tail(Object value, List<Object> expected) {
        var args = new ArrayList<>();
        args.add(value);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("tailCases")
    void tailViaPipe(Object value, List<Object> expected) {
        var actual = function.invoke(List.of(), value);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> tailCases() {
        return Stream.of(
                Arguments.of(null, List.of()),
                Arguments.of(List.of(), List.of()),
                Arguments.of(new Object[0], List.of()),

                Arguments.of(List.of(""), List.of()),
                Arguments.of(new String[]{""}, List.of()),

                Arguments.of(List.of("hello"), List.of()),
                Arguments.of(new String[]{"hello"}, List.of()),

                Arguments.of(List.of("hello", "world"), List.of("world")),
                Arguments.of(new String[]{"hello", "world"}, List.of("world"))
        );
    }

}
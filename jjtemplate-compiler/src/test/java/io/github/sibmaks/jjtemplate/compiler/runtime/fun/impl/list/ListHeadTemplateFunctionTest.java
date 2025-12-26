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
class ListHeadTemplateFunctionTest {
    @InjectMocks
    private ListHeadTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("list", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("head", actual);
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
        assertEquals("list:head: unsupported type: " + Boolean.class, exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:head: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("list:head: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:head: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("headCases")
    void head(Object value, Object expected) {
        var args = new ArrayList<>();
        args.add(value);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("headCases")
    void headViaPipe(Object value, Object expected) {
        var actual = function.invoke(List.of(), value);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> headCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(List.of(), null),
                Arguments.of(new Object[0], null),

                Arguments.of(List.of(""), ""),
                Arguments.of(new String[]{""}, ""),

                Arguments.of(List.of("hello"), "hello"),
                Arguments.of(new String[]{"hello"}, "hello"),

                Arguments.of(List.of("hello", "world"), "hello"),
                Arguments.of(new String[]{"hello", "world"}, "hello")
        );
    }

}
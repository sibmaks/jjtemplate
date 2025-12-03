package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

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
class StringSubstringTemplateFunctionTest {
    @InjectMocks
    private StringSubstringTemplateFunction function;

    static Stream<Arguments> substrCases() {
        return Stream.of(
                Arguments.of(null, 0, null, null),
                Arguments.of("hello", 0, null, "hello"),
                Arguments.of("hello", 1, null, "ello"),
                Arguments.of("hello", 4, null, "o"),
                Arguments.of("hello", 0, 5, "hello"),
                Arguments.of("hello", 1, 3, "el"),
                Arguments.of("abcdef", 2, 4, "cd"),
                Arguments.of("abcdef", 0, 10, "abcdef"),
                Arguments.of("abcdef", 10, 12, ""),
                Arguments.of("test", 0, 0, ""), // empty substr,

                // begin < 0
                Arguments.of("hello", -1, null, "o"),
                Arguments.of("hello", -2, null, "lo"),
                Arguments.of("hello", -5, null, "hello"),
                Arguments.of("hello", -10, null, "hello"),

                // end < 0
                Arguments.of("hello", 0, -1, "hell"),
                Arguments.of("hello", 1, -1, "ell"),
                Arguments.of("hello", 0, -2, "hel"),

                // begin < 0 and end < 0
                Arguments.of("abcdef", -4, -1, "cde"),
                Arguments.of("abcdef", -3, -2, "d"),

                Arguments.of("test", -1, -1, ""),
                Arguments.of("test", -5, -1, "tes"),
                Arguments.of("test", -7, -5, ""),
                Arguments.of("test", -4, -4, "")
        );
    }

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("substr", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @ParameterizedTest
    @MethodSource("substrCases")
    void directInvoke(String value, int begin, Integer end, String expected) {
        var args = new ArrayList<>();
        args.add(value);
        args.add(begin);

        if (end != null) {
            args.add(end);
        }

        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void directInvokeNullValueReturnsNull() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(1);
        var actual = function.invoke(args);
        assertNull(actual);
    }

    @Test
    void directInvokeTooFewArgs() {
        var args = List.<Object>of("hello");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: at least 2 arguments required", ex.getMessage());
    }

    @Test
    void directInvokeTooManyArgs() {
        var args = List.<Object>of("a", 1, 2, 3);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: too much arguments passed", ex.getMessage());
    }

    @Test
    void directInvokeEndIndexLessThanBegin() {
        var args = List.<Object>of("hello", 3, 2);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: endIndex < beginIndex after normalization", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("substrCases")
    void pipeInvoke(String value, int begin, Integer end, String expected) {
        List<Object> args;
        if (end == null) {
            args = List.of(begin);
        } else {
            args = List.of(begin, end);
        }

        var actual = function.invoke(args, value);
        assertEquals(expected, actual);
    }

    @Test
    void pipeInvokeNullValueReturnsNull() {
        var args = List.<Object>of(1);
        var actual = function.invoke(args, null);
        assertNull(actual);
    }

    @Test
    void pipeInvokeTooFewArgs() {
        var args = List.of();
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: at least 1 argument required", ex.getMessage());
    }

    @Test
    void pipeInvokeTooManyArgs() {
        var args = List.<Object>of(1, 2, 3);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: too much arguments passed", ex.getMessage());
    }

    @Test
    void pipeInvokeEndIndexLessThanBegin() {
        var args = List.<Object>of(3, 1);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: endIndex < beginIndex after normalization", ex.getMessage());
    }

    @Test
    void directInvokeNegativeIndexesBecomeInvalidAfterNormalization() {
        var args = List.<Object>of("hello", -1, 1);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: endIndex < beginIndex after normalization", ex.getMessage());
    }

    @Test
    void pipeInvokeNegativeIndexesBecomeInvalidAfterNormalization() {
        var args = List.<Object>of(-1, 1);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: endIndex < beginIndex after normalization", ex.getMessage());
    }


}
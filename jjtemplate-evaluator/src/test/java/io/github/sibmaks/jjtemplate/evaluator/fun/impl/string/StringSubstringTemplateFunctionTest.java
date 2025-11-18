package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
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
                Arguments.of("hello", 0, null, "hello"),
                Arguments.of("hello", 1, null, "ello"),
                Arguments.of("hello", 4, null, "o"),
                Arguments.of("hello", 0, 5, "hello"),
                Arguments.of("hello", 1, 3, "el"),
                Arguments.of("abcdef", 2, 4, "cd"),
                Arguments.of("test", 0, 0, "") // empty substr
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

    @ParameterizedTest
    @MethodSource("substrCases")
    void directInvoke(String value, int begin, Integer end, String expected) {
        List<Object> args;
        if (end == null) {
            args = List.of(value, begin);
        } else {
            args = List.of(value, begin, end);
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
    void directInvokeBeginIndexOutOfBounds() {
        var args = List.<Object>of("hello", 10);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: beginIndex out of bounds", ex.getMessage());
    }

    @Test
    void directInvokeEndIndexOutOfBounds() {
        var args = List.<Object>of("hello", 1, 10);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: endIndex out of bounds", ex.getMessage());
    }

    @Test
    void directInvokeEndIndexLessThanBegin() {
        var args = List.<Object>of("hello", 3, 2);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:substr: endIndex out of bounds", ex.getMessage());
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
    void pipeInvokeBeginIndexOutOfBounds() {
        var args = List.<Object>of(10);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: beginIndex out of bounds", ex.getMessage());
    }

    @Test
    void pipeInvokeEndIndexOutOfBounds() {
        var args = List.<Object>of(1, 10);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: endIndex out of bounds", ex.getMessage());
    }

    @Test
    void pipeInvokeEndIndexLessThanBegin() {
        var args = List.<Object>of(3, 1);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:substr: endIndex out of bounds", ex.getMessage());
    }

}
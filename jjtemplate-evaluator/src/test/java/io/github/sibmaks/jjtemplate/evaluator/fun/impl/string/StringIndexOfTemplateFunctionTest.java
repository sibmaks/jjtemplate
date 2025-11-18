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
class StringIndexOfTemplateFunctionTest {
    @InjectMocks
    private StringIndexOfTemplateFunction function;

    static Stream<Arguments> indexCases() {
        return Stream.of(
                Arguments.of("hello world", "h", 0),
                Arguments.of("hello world", "world", 6),
                Arguments.of("hello", "lo", 3),
                Arguments.of("hello", "x", -1),
                Arguments.of("aaaaa", "aa", 0),
                Arguments.of("abc", "", 0) // indexOf("") = 0
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("indexOf", actual);
    }

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @ParameterizedTest
    @MethodSource("indexCases")
    void directInvoke(String value, String find, int expected) {
        var args = List.<Object>of(value, find);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @Test
    void directInvokeNullValueReturnsNull() {
        var args = new ArrayList<>();
        args.add(null);
        args.add("x");
        var result = function.invoke(args);
        assertNull(result);
    }

    @Test
    void directInvokeNotEnoughArgs() {
        var args = List.<Object>of("hello");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:indexOf: 2 arguments required", ex.getMessage());
    }

    @Test
    void directInvokeTooManyArgs() {
        var args = List.<Object>of("a", "b", "c");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:indexOf: 2 arguments required", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("indexCases")
    void pipeInvoke(String value, String find, int expected) {
        var args = List.<Object>of(find);
        var result = function.invoke(args, value);
        assertEquals(expected, result);
    }

    @Test
    void pipeInvokeNullPipeReturnsNull() {
        var args = List.<Object>of("x");
        var result = function.invoke(args, null);
        assertNull(result);
    }

    @Test
    void pipeInvokeWrongArgsCount() {
        var args = List.<Object>of("a", "b");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:indexOf: 1 argument required", ex.getMessage());
    }
}
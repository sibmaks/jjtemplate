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
class StringReplaceAllTemplateFunctionTest {
    @InjectMocks
    private StringReplaceAllTemplateFunction function;

    static Stream<Arguments> replaceAllCases() {
        return Stream.of(
                Arguments.of("pello", "p", "h", "hello"),
                Arguments.of("pello", "[p]+", "h", "hello"),
                Arguments.of("", "p", "h", "")
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("replaceAll", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @ParameterizedTest
    @MethodSource("replaceAllCases")
    void directInvoke(String value, String target, String replacement, String expected) {
        var args = List.<Object>of(value, target, replacement);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @Test
    void directInvokeNullValueReturnsNull() {
        var args = new ArrayList<>();
        args.add(null);
        args.add("x");
        args.add("y");
        var result = function.invoke(args);
        assertNull(result);
    }

    @Test
    void directInvokeNotEnoughArgs() {
        var args = List.<Object>of("hello", "world");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:replaceAll: 3 arguments required", ex.getMessage());
    }

    @Test
    void directInvokeTooManyArgs() {
        var args = List.<Object>of("a", "b", "c", "d");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:replaceAll: 3 arguments required", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("replaceAllCases")
    void pipeInvoke(String value, String target, String replacement, String expected) {
        var args = List.<Object>of(target, replacement);
        var result = function.invoke(args, value);
        assertEquals(expected, result);
    }

    @Test
    void pipeInvokeNullPipeReturnsNull() {
        var args = List.<Object>of("x","y");
        var result = function.invoke(args, null);
        assertNull(result);
    }

    @Test
    void pipeInvokeWrongArgsCount() {
        var args = List.<Object>of("a");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "hello"));
        assertEquals("string:replaceAll: 2 arguments required", ex.getMessage());
    }
}

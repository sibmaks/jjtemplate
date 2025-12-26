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
class StringLastIndexOfTemplateFunctionTest {
    @InjectMocks
    private StringLastIndexOfTemplateFunction function;

    static Stream<Arguments> lastIndexCases() {
        return Stream.of(
                Arguments.of("hello world", "h", 0),
                Arguments.of("hello world", "o", 7),
                Arguments.of("hello", "l", 3),
                Arguments.of("hello", "lo", 3),
                Arguments.of("aaaaa", "aa", 3),
                Arguments.of("abc", "", 3),   // lastIndexOf("") = length()
                Arguments.of("abc", "x", -1)
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("lastIndexOf", actual);
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

    @Test
    void checkNamespaceAndName() {
        assertEquals("string", function.getNamespace());
        assertEquals("lastIndexOf", function.getName());
    }

    @ParameterizedTest
    @MethodSource("lastIndexCases")
    void directInvoke(String value, String find, int expected) {
        var args = List.<Object>of(value, find);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void directInvokeNullValueReturnsNull() {
        var args = new ArrayList<>();
        args.add(null);
        args.add("x");
        var actual = function.invoke(args);
        assertNull(actual);
    }

    @Test
    void directInvokeWrongArgsCount() {
        var args = List.<Object>of("a");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:lastIndexOf: 2 arguments required", ex.getMessage());
    }

    @Test
    void directInvokeTooManyArgs() {
        var args = List.<Object>of("a", "b", "c");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:lastIndexOf: 2 arguments required", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("lastIndexCases")
    void pipeInvoke(String value, String find, int expected) {
        var args = List.<Object>of(find);
        var actual = function.invoke(args, value);
        assertEquals(expected, actual);
    }

    @Test
    void pipeInvokeNullPipeReturnsNull() {
        var args = List.<Object>of("x");
        var actual = function.invoke(args, null);
        assertNull(actual);
    }

    @Test
    void pipeInvokeWrongArgsCount() {
        var args = List.<Object>of("a", "b");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "value"));
        assertEquals("string:lastIndexOf: 1 argument required", ex.getMessage());
    }
}
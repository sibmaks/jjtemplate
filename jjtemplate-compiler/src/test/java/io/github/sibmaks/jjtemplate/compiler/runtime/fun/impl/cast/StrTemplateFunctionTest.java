package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.cast;

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
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StrTemplateFunctionTest {
    @InjectMocks
    private StrTemplateFunction function;

    public static Stream<Arguments> justToStringCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(42, "42"),
                Arguments.of(42.31, "42.31"),
                Arguments.of(true, "true"),
                Arguments.of(false, "false"),
                Arguments.of("text", "text")
        );
    }

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("cast", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("str", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void nullPipeInvoke() {
        var actual = function.invoke(List.of(), null);
        assertNull(actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("cast:str: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:str: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:str: 1 argument required", exception.getMessage());
    }

    @Test
    void nullArgInvoke() {
        var args = new ArrayList<>();
        args.add(null);
        var actual = function.invoke(args);
        assertNull(actual);
    }

    @ParameterizedTest
    @MethodSource("justToStringCases")
    void justToStringArg(Object input, String excepted) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(excepted, actual);
    }

    @ParameterizedTest
    @MethodSource("justToStringCases")
    void justToStringPipe(Object input, String excepted) {
        var actual = function.invoke(List.of(), input);
        assertEquals(excepted, actual);
    }
}
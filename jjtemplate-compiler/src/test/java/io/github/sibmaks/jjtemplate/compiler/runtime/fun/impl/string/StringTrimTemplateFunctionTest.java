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
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StringTrimTemplateFunctionTest {
    @InjectMocks
    private StringTrimTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("trim", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(Locale.US, Locale.UK);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("string:trim: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:trim: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of("a", "b");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:trim: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("trimCases")
    void trim(Object input, String expected) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("trimCases")
    void trimAsPipe(Object input, String expected) {
        var actual = function.invoke(List.of(), input);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> trimCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("hello", "hello"),
                Arguments.of("right ", "right"),
                Arguments.of(" left", "left"),
                Arguments.of(" both ", "both")
        );
    }


}
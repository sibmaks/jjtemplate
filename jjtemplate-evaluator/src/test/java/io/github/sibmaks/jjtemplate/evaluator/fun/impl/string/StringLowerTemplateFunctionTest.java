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
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StringLowerTemplateFunctionTest {
    @InjectMocks
    private StringLowerTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("lower", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(Locale.US, Locale.UK);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("string:lower: too much arguments passed", exception.getMessage());
    }

    @Test
    void defaultLocaleOnArgsInvoke() {
        var args = List.<Object>of("PIPE");
        var actual = function.invoke(args);
        assertEquals("pipe", actual);
    }

    @Test
    void defaultLocaleOnPipeInvoke() {
        var args = List.of();
        var actual = function.invoke(args, "PIPE");
        assertEquals("pipe", actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:lower: at least 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(Locale.US, "A", "B");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:lower: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("lowerCases")
    void lower(Object input, String expected) {
        var args = new ArrayList<>();
        args.add(Locale.US);
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("lowerCases")
    void lowerAsPipe(Object input, String expected) {
        var actual = function.invoke(List.of(Locale.US), input);
        assertEquals(expected, actual);
    }

    @Test
    void lowerWithLocale() {
        var args = List.<Object>of(Locale.FRANCE, "BONJOUR");
        var actual = function.invoke(args);
        assertEquals("bonjour", actual);
    }

    public static Stream<Arguments> lowerCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(42, "42"),
                Arguments.of(3.1415, "3.1415"),
                Arguments.of(true, "true"),
                Arguments.of(false, "false"),
                Arguments.of("HELLO", "hello"),
                Arguments.of("world", "world")
        );
    }

}
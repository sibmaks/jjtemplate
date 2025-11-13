package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
class StringUpperTemplateFunctionTest {
    @InjectMocks
    private StringUpperTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("upper", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(Locale.US, Locale.UK);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("upper: too much arguments passed", exception.getMessage());
    }

    @Test
    void defaultLocaleOnArgsInvoke() {
        var args = List.<Object>of("pipe");
        var actual = function.invoke(args);
        assertEquals("PIPE", actual);
    }

    @Test
    void defaultLocaleOnPipeInvoke() {
        var args = List.of();
        var actual = function.invoke(args, "pipe");
        assertEquals("PIPE", actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("upper: at least 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(Locale.US, "a", "b");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("upper: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("upperCases")
    void upper(Object input, String expected) {
        var args = new ArrayList<>();
        args.add(Locale.US);
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("upperCases")
    void upperAsPipe(Object input, String expected) {
        var actual = function.invoke(List.of(Locale.US), input);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> upperCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(42, "42"),
                Arguments.of(3.1415, "3.1415"),
                Arguments.of(true, "TRUE"),
                Arguments.of(false, "FALSE"),
                Arguments.of("hello", "HELLO"),
                Arguments.of("WORLD", "WORLD")
        );
    }
}

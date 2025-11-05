package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author sibmaks
 */
class StringLowerTemplateFunctionTest {
    private final TemplateFunction<String> function = new StringLowerTemplateFunction(Locale.US);

    public static Stream<Arguments> lowerCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(42, "42"),
                Arguments.of(3.1415, "3.1415"),
                Arguments.of(true, "true"),
                Arguments.of(false, "false"),
                Arguments.of("hello", "hello"),
                Arguments.of("WORLD", "world")
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("lower", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("lower: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("lower: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("lower: 1 argument required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("lowerCases")
    void lower(Object input, String expected) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("lowerCases")
    void lowerAsPipe(Object input, String expected) {
        var actual = function.invoke(List.of(), input);
        assertEquals(expected, actual);
    }


}
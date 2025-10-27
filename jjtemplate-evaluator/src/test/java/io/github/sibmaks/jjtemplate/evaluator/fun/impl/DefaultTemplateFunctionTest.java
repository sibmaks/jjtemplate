package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class DefaultTemplateFunctionTest {
    @InjectMocks
    private DefaultTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("default", actual);
    }

    @Test
    void checkWhenNoArgs() {
        var pipe = ExpressionValue.of(42);
        var actual = function.invoke(List.of(), pipe);
        assertEquals(pipe, actual);
    }

    @Test
    void checkWhenTooManyArgs() {
        var args = List.of(ExpressionValue.of(42), ExpressionValue.of(43));
        var pipe = ExpressionValue.of(44);
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("default: 2 arguments required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("checkCases")
    void checkWhenOneArg(Object arg, Object pipe, Object expected) {
        var actual = function.invoke(
                List.of(
                        ExpressionValue.of(arg)
                ),
                ExpressionValue.of(pipe)
        );
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("checkCases")
    void checkWhenTwoArgs(Object arg, Object pipe, Object expected) {
        var actual = function.invoke(
                List.of(
                        ExpressionValue.of(pipe),
                        ExpressionValue.of(arg)
                ),
                ExpressionValue.empty()
        );
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    public static Stream<Arguments> checkCases() {
        return Stream.of(
                Arguments.of("fail", "ok", "ok"),
                Arguments.of("ok", null, "ok"),
                Arguments.of(null, "ok", "ok")
        );
    }
}
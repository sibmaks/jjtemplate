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
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class LengthTemplateFunctionTest {
    @InjectMocks
    private LengthTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("len", actual);
    }

    @Test
    void notArgumentsIsTrue() {
        var actual = function.invoke(List.of(), ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(0, actual.getValue());
    }

    @Test
    void unsupportedArgumentType() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(String.class);

        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("len: unsupported type: " + Class.class, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("emptyCases")
    void length(Object value, int expected) {
        var actual = function.invoke(
                List.of(ExpressionValue.of(value)),
                ExpressionValue.empty()
        );
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("emptyCases")
    void lengthViaPipe(Object value, int expected) {
        var actual = function.invoke(
                List.of(),
                ExpressionValue.of(value)
        );
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    public static Stream<Arguments> emptyCases() {
        return Stream.of(
                Arguments.of(null, 0),
                Arguments.of("", 0),
                Arguments.of(List.of(), 0),
                Arguments.of(Map.of(), 0),
                Arguments.of(new Object[0], 0),

                Arguments.of("text", 4),
                Arguments.of(List.of(""), 1),
                Arguments.of(Map.of("key", "value"), 1),
                Arguments.of(new Object[1], 1)
        );
    }

}
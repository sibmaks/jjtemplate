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
class EmptyTemplateFunctionTest {
    @InjectMocks
    private EmptyTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("empty", actual);
    }

    @Test
    void notArgumentsIsTrue() {
        var actual = function.invoke(List.of(), ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void unsupportedArgumentType() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(
                        List.of(),
                        ExpressionValue.of(String.class)
                )
        );
        assertEquals("empty: unsupported type: " + Class.class, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("emptyCases")
    void empty(Object value, boolean expected) {
        var actual = function.invoke(
                List.of(ExpressionValue.of(value)),
                ExpressionValue.empty()
        );
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("emptyCases")
    void emptyViaPipe(Object value, boolean expected) {
        var actual = function.invoke(
                List.of(),
                ExpressionValue.of(value)
        );
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    public static Stream<Arguments> emptyCases() {
        return Stream.of(
                Arguments.of(null, true),
                Arguments.of("", true),
                Arguments.of(List.of(), true),
                Arguments.of(Map.of(), true),
                Arguments.of(new Object[0], true),

                Arguments.of("text", false),
                Arguments.of(List.of(""), false),
                Arguments.of(Map.of("key", "value"), false),
                Arguments.of(new Object[1], false)
        );
    }
}
package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class OrTemplateFunctionTest {
    @InjectMocks
    private OrTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("or", actual);
    }

    @Test
    void withoutArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
        );
        assertEquals("or: 2 arguments required", exception.getMessage());
    }

    @Test
    void withOnlyOneArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(ExpressionValue.of(42)), ExpressionValue.empty())
        );
        assertEquals("or: 2 arguments required", exception.getMessage());
    }

    @Test
    void withOnlyOnePipeArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.of(42))
        );
        assertEquals("or: 2 arguments required", exception.getMessage());
    }

    @Test
    void withTooMuchArguments() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(
                        ExpressionValue.of(42),
                        ExpressionValue.of(42)
                ), ExpressionValue.of(42))
        );
        assertEquals("or: 2 arguments required", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void withNotBooleanArgument(boolean first) {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(
                        ExpressionValue.of(first ? 42 : true),
                        ExpressionValue.of(first ? true : 42)
                ), ExpressionValue.empty())
        );
        assertEquals("or: All arguments must be a boolean", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passAsArguments(boolean left, boolean right, boolean expected) {
        var actual = function.invoke(List.of(ExpressionValue.of(left), ExpressionValue.of(right)), ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passWithPipe(boolean left, boolean right, boolean expected) {
        var actual = function.invoke(List.of(ExpressionValue.of(left)), ExpressionValue.of(right));
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    public static Stream<Arguments> cmpCases() {
        return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(true, false, true),
                Arguments.of(false, true, true),
                Arguments.of(false, false, false)
        );
    }

}
package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

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
class EqualsTemplateFunctionTest {
    @InjectMocks
    private EqualsTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("eq", actual);
    }

    @Test
    void withoutArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
        );
        assertEquals("eq: 2 arguments required", exception.getMessage());
    }

    @Test
    void withOnlyOneArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(ExpressionValue.of(42)), ExpressionValue.empty())
        );
        assertEquals("eq: 2 arguments required", exception.getMessage());
    }

    @Test
    void withOnlyOnePipeArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.of(42))
        );
        assertEquals("eq: 2 arguments required", exception.getMessage());
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
        assertEquals("eq: 2 arguments required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passAsArguments(Object left, Object right, boolean expected) {
        var actual = function.invoke(List.of(ExpressionValue.of(left), ExpressionValue.of(right)), ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passWithPipe(Object left, Object right, boolean expected) {
        var actual = function.invoke(List.of(ExpressionValue.of(left)), ExpressionValue.of(right));
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.getValue());
    }

    public static Stream<Arguments> cmpCases() {
        var object = new Object();
        return Stream.of(
                Arguments.of(null, null, true),
                Arguments.of("text", null, false),
                Arguments.of(null, "text", false),
                Arguments.of("text", "text", true),
                Arguments.of(42, 42, true),
                Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE, true),
                Arguments.of(3.1415, 3.1415, true),
                Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, true),
                Arguments.of(object, object, true),
                Arguments.of(object, new Object(), false)
        );
    }
}
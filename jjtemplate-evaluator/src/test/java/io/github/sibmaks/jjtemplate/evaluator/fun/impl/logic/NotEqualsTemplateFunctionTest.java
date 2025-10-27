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
class NotEqualsTemplateFunctionTest {
    @InjectMocks
    private NotEqualsTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("neq", actual);
    }

    @Test
    void withoutArgument() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("neq: 2 arguments required", exception.getMessage());
    }

    @Test
    void withOnlyOneArgument() {
        var args = List.of(ExpressionValue.of(42));
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("neq: 2 arguments required", exception.getMessage());
    }

    @Test
    void withOnlyOnePipeArgument() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(42);
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("neq: 2 arguments required", exception.getMessage());
    }

    @Test
    void withTooMuchArguments() {
        var args = List.of(
                ExpressionValue.of(42),
                ExpressionValue.of(43)
        );
        var pipe = ExpressionValue.of(44);
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("neq: 2 arguments required", exception.getMessage());
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
                Arguments.of(null, null, false),
                Arguments.of("text", null, true),
                Arguments.of(null, "text", true),
                Arguments.of("text", "text", false),
                Arguments.of(42, 42, false),
                Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE, false),
                Arguments.of(3.1415, 3.1415, false),
                Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, false),
                Arguments.of(object, object, false),
                Arguments.of(object, new Object(), true)
        );
    }

}
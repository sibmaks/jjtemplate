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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class LECompareTemplateFunctionTest {
    @InjectMocks
    private LECompareTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("le", actual);
    }

    @Test
    void withoutArguments() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
        );
        assertEquals("cmp: invalid args", exception.getMessage());
    }

    @Test
    void withoutPipeArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(ExpressionValue.of(null)), ExpressionValue.empty())
        );
        assertEquals("cmp: invalid args", exception.getMessage());
    }

    @Test
    void withALotOfArguments() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(
                        List.of(
                                ExpressionValue.of(null), ExpressionValue.of(null)
                        ),
                        ExpressionValue.of(null)
                )
        );
        assertEquals("cmp: invalid args", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void cmp(Number left, Number right, boolean excepted) {
        var actual = function.invoke(
                List.of(
                        ExpressionValue.of(left),
                        ExpressionValue.of(right)
                ), ExpressionValue.empty()
        );
        assertFalse(actual.isEmpty());
        assertEquals(excepted, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void cmpWithPipe(Number left, Number right, boolean excepted) {
        var actual = function.invoke(
                List.of(
                        ExpressionValue.of(right)
                ), ExpressionValue.of(left)
        );
        assertFalse(actual.isEmpty());
        assertEquals(excepted, actual.getValue());
    }

    public static Stream<Arguments> cmpCases() {
        return Stream.of(
                Arguments.of(0, 1, true),
                Arguments.of(0, -1, false),
                Arguments.of(1, -1, false),
                Arguments.of(-1, 1, true),
                Arguments.of(0, 0, true),
                Arguments.of(Long.MIN_VALUE, Long.MAX_VALUE, true),
                Arguments.of(Long.MAX_VALUE, Long.MIN_VALUE, false),
                Arguments.of(Double.MIN_VALUE, Double.MAX_VALUE, true),
                Arguments.of(Double.MAX_VALUE, Double.MIN_VALUE, false),
                Arguments.of(BigDecimal.valueOf(Double.MIN_VALUE), BigDecimal.valueOf(Double.MAX_VALUE), true),
                Arguments.of(BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.valueOf(Double.MIN_VALUE), false)
        );
    }
}
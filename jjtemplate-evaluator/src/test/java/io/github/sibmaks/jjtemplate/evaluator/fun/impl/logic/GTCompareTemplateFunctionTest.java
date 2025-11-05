package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class GTCompareTemplateFunctionTest {

    @InjectMocks
    private GTCompareTemplateFunction function;

    public static Stream<Arguments> cmpCases() {
        return Stream.of(
                Arguments.of(0, 1, false),
                Arguments.of(0, -1, true),
                Arguments.of(1, -1, true),
                Arguments.of(-1, 1, false),
                Arguments.of(0, 0, false),
                Arguments.of(Long.MIN_VALUE, Long.MAX_VALUE, false),
                Arguments.of(Long.MAX_VALUE, Long.MIN_VALUE, true),
                Arguments.of(Double.MIN_VALUE, Double.MAX_VALUE, false),
                Arguments.of(Double.MAX_VALUE, Double.MIN_VALUE, true),
                Arguments.of(BigDecimal.valueOf(Double.MIN_VALUE), BigDecimal.valueOf(Double.MAX_VALUE), false),
                Arguments.of(BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.valueOf(Double.MIN_VALUE), true)
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("gt", actual);
    }

    @Test
    void withALotOfArguments() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(null);
        args.add(null);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("gt: 2 arguments required", exception.getMessage());
    }

    @Test
    void withALotOfArgumentsPipe() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(null);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("gt: 1 argument required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void cmp(Number left, Number right, boolean excepted) {
        var actual = function.invoke(List.of(left, right));
        assertEquals(excepted, actual);
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void cmpWithPipe(Number left, Number right, boolean excepted) {
        var actual = function.invoke(List.of(right), left);
        assertEquals(excepted, actual);
    }
}
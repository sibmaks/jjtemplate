package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class NegTemplateFunctionTest {
    @InjectMocks
    private NegTemplateFunction function;


    public static Stream<Arguments> negCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(42, new BigInteger("-42")),
                Arguments.of(3.F, new BigDecimal("-3.0")),
                Arguments.of(3.1415, new BigDecimal("-3.1415")),
                Arguments.of(new BigInteger("-42"), new BigInteger("42")),
                Arguments.of(new BigDecimal("-3.1415"), new BigDecimal("3.1415"))
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("neg", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("math:neg: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:neg: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:neg: 1 argument required", exception.getMessage());
    }

    @Test
    void notANumber() {
        var item = UUID.randomUUID().toString();
        var args = List.<Object>of(item);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:neg: not a number passed: " + item, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("negCases")
    void neg(Object input, Object expected) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("negCases")
    void negAsPipe(Object input, Object expected) {
        var actual = function.invoke(List.of(), input);
        assertEquals(expected, actual);
    }
}
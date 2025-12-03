package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class SumTemplateFunctionTest {
    @InjectMocks
    private SumTemplateFunction function;


    @Test
    void checkNamespace() {
        assertEquals("math", function.getNamespace());
    }

    @Test
    void checkName() {
        assertEquals("sum", function.getName());
    }

    @ParameterizedTest
    @MethodSource("cases")
    void sumAsArgs(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void sumAsPipe(Number left, Number right, Number expected) {
        var args = List.<Object>of(left);
        var result = function.invoke(args, right);
        assertEquals(expected, result);
    }

    public static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(-1, 1, BigInteger.valueOf(0)),
                Arguments.of(1, -1, BigInteger.valueOf(0)),
                Arguments.of(1, 1, BigInteger.valueOf(2)),
                Arguments.of(-1, -1, BigInteger.valueOf(-2)),

                Arguments.of(BigInteger.valueOf(-1), BigInteger.valueOf(1), BigInteger.valueOf(0)),
                Arguments.of(BigInteger.valueOf(1), BigInteger.valueOf(-1), BigInteger.valueOf(0)),
                Arguments.of(BigInteger.valueOf(1), BigInteger.valueOf(1), BigInteger.valueOf(2)),
                Arguments.of(BigInteger.valueOf(-1), BigInteger.valueOf(-1), BigInteger.valueOf(-2)),

                Arguments.of(BigInteger.valueOf(-1), 1, BigInteger.valueOf(0)),
                Arguments.of(BigInteger.valueOf(1), -1, BigInteger.valueOf(0)),
                Arguments.of(BigInteger.valueOf(1), 1, BigInteger.valueOf(2)),
                Arguments.of(BigInteger.valueOf(-1), -1, BigInteger.valueOf(-2)),

                Arguments.of(-1, BigInteger.valueOf(1), BigInteger.valueOf(0)),
                Arguments.of(1, BigInteger.valueOf(-1), BigInteger.valueOf(0)),
                Arguments.of(1, BigInteger.valueOf(1), BigInteger.valueOf(2)),
                Arguments.of(-1, BigInteger.valueOf(-1), BigInteger.valueOf(-2)),

                Arguments.of(-1., 1, BigDecimal.valueOf(0.0)),
                Arguments.of(1., -1, BigDecimal.valueOf(0.0)),
                Arguments.of(1., 1, BigDecimal.valueOf(2.0)),
                Arguments.of(-1., -1, BigDecimal.valueOf(-2.0)),

                Arguments.of(-1.f, 1, BigDecimal.valueOf(0.0)),
                Arguments.of(1.f, -1, BigDecimal.valueOf(0.0)),
                Arguments.of(1.f, 1, BigDecimal.valueOf(2.0)),
                Arguments.of(-1.f, -1, BigDecimal.valueOf(-2.0)),

                Arguments.of(BigDecimal.valueOf(-1.), 1, BigDecimal.valueOf(0.0)),
                Arguments.of(BigDecimal.valueOf(1.), -1, BigDecimal.valueOf(0.0)),
                Arguments.of(BigDecimal.valueOf(1.), 1, BigDecimal.valueOf(2.0)),
                Arguments.of(BigDecimal.valueOf(-1.), -1, BigDecimal.valueOf(-2.0)),

                Arguments.of(-1, 1., BigDecimal.valueOf(0.0)),
                Arguments.of(1, -1., BigDecimal.valueOf(0.0)),
                Arguments.of(1, 1., BigDecimal.valueOf(2.0)),
                Arguments.of(-1, -1., BigDecimal.valueOf(-2.0)),

                Arguments.of(-1, 1.f, BigDecimal.valueOf(0.0)),
                Arguments.of(1, -1.f, BigDecimal.valueOf(0.0)),
                Arguments.of(1, 1.f, BigDecimal.valueOf(2.0)),
                Arguments.of(-1, -1.f, BigDecimal.valueOf(-2.0)),

                Arguments.of(-1., 1., BigDecimal.valueOf(0.0)),
                Arguments.of(1., -1., BigDecimal.valueOf(0.0)),
                Arguments.of(1., 1., BigDecimal.valueOf(2.0)),
                Arguments.of(-1., -1., BigDecimal.valueOf(-2.0)),

                Arguments.of(-1., BigDecimal.valueOf(1.), BigDecimal.valueOf(0.0)),
                Arguments.of(1., BigDecimal.valueOf(-1.), BigDecimal.valueOf(0.0)),
                Arguments.of(1., BigDecimal.valueOf(1.), BigDecimal.valueOf(2.0)),
                Arguments.of(-1., BigDecimal.valueOf(-1.), BigDecimal.valueOf(-2.0)),

                Arguments.of(-1.f, 1.f, BigDecimal.valueOf(0.0)),
                Arguments.of(1.f, -1.f, BigDecimal.valueOf(0.0)),
                Arguments.of(1.f, 1.f, BigDecimal.valueOf(2.0)),
                Arguments.of(-1.f, -1.f, BigDecimal.valueOf(-2.0)),

                Arguments.of(BigDecimal.valueOf(-1.), BigDecimal.valueOf(1.), BigDecimal.valueOf(0.0)),
                Arguments.of(BigDecimal.valueOf(1.), BigDecimal.valueOf(-1.), BigDecimal.valueOf(0.0)),
                Arguments.of(BigDecimal.valueOf(1.), BigDecimal.valueOf(1.), BigDecimal.valueOf(2.0)),
                Arguments.of(BigDecimal.valueOf(-1.), BigDecimal.valueOf(-1.), BigDecimal.valueOf(-2.0)),

                Arguments.of(BigInteger.valueOf(-1), BigDecimal.valueOf(1.), BigDecimal.valueOf(0.0)),
                Arguments.of(BigDecimal.valueOf(1.), BigInteger.valueOf(-1), BigDecimal.valueOf(0.0)),
                Arguments.of(BigInteger.valueOf(1), BigDecimal.valueOf(1.), BigDecimal.valueOf(2.0)),
                Arguments.of(BigDecimal.valueOf(-1.), BigInteger.valueOf(-1), BigDecimal.valueOf(-2.0))

        );
    }

    @Test
    void sumNullValue() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(5);
        var result = function.invoke(args);
        assertNull(result);
    }

    @Test
    void sumNullPipe() {
        var args = List.<Object>of(10);
        var result = function.invoke(args, null);
        assertNull(result);
    }

    @Test
    void invalidArgsType() {
        var args = List.<Object>of("not", 1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:sum: not a number passed", exception.getMessage());
    }

    @Test
    void invalidPipeArgType() {
        var args = List.<Object>of(1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "x"));
        assertEquals("math:sum: not a number passed", exception.getMessage());
    }

    @Test
    void tooManyArgsInInvoke() {
        var args = List.<Object>of(1, 2, 3);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:sum: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooFewArgsInInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:sum: 2 arguments required", exception.getMessage());
    }

    @Test
    void tooManyArgsInPipeInvoke() {
        var args = List.<Object>of(1, 2);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 3));
        assertEquals("math:sum: too much arguments passed", exception.getMessage());
    }
}
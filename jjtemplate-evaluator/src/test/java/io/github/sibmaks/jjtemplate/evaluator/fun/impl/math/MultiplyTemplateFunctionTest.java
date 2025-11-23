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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class MultiplyTemplateFunctionTest {
    @InjectMocks
    private MultiplyTemplateFunction function;


    @Test
    void checkNamespace() {
        assertEquals("math", function.getNamespace());
    }

    @Test
    void checkName() {
        assertEquals("mul", function.getName());
    }

    @ParameterizedTest
    @MethodSource("cases")
    void multiplyAsArgs(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void multiplyAsPipe(Number left, Number right, Number expected) {
        var args = List.<Object>of(left);
        var result = function.invoke(args, right);
        assertEquals(expected, result);
    }

    @Test
    void multiplyNullValue() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(5);
        var result = function.invoke(args);
        assertNull(result);
    }

    @Test
    void multiplyNullPipe() {
        var args = List.<Object>of(10);
        var result = function.invoke(args, null);
        assertNull(result);
    }

    @Test
    void invalidArgsType() {
        var args = List.<Object>of("not", 1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:mul: not a number passed", exception.getMessage());
    }

    @Test
    void invalidPipeArgType() {
        var args = List.<Object>of(1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "x"));
        assertEquals("math:mul: not a number passed", exception.getMessage());
    }

    @Test
    void tooManyArgsInInvoke() {
        var args = List.<Object>of(1, 2, 3);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:mul: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooFewArgsInInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:mul: 2 arguments required", exception.getMessage());
    }

    @Test
    void tooManyArgsInPipeInvoke() {
        var args = List.<Object>of(1, 2);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 3));
        assertEquals("math:mul: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("bigDecimalCases")
    void multiplyBigDecimal(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("bigIntegerCases")
    void multiplyBigInteger(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("floatDoubleCases")
    void multiplyFloatDouble(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @Test
    void multiplyZero() {
        var args = List.<Object>of(0, 999);
        var result = function.invoke(args);
        assertEquals(BigInteger.ZERO, result);
    }

    @Test
    void multiplyNegativeZero() {
        var args = List.<Object>of(-0.0, 5);
        var result = function.invoke(args);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void multiplyHugeBigInteger() {
        var left = new BigInteger("999999999999999999999");
        var right = new BigInteger("1000000000000000000000");
        var result = function.invoke(List.of(left, right));
        assertEquals(left.multiply(right), result);
    }

    public static Stream<Arguments> bigDecimalCases() {
        return Stream.of(
                Arguments.of(new BigDecimal("1.5"), new BigDecimal("2.0"), new BigDecimal("3.00")),
                Arguments.of(new BigDecimal("1.5"), 2, new BigDecimal("3.00")),
                Arguments.of(2, new BigDecimal("1.5"), new BigDecimal("3.00")),
                Arguments.of(new BigDecimal("0.1"), new BigDecimal("0.2"), new BigDecimal("0.02"))
        );
    }

    public static Stream<Arguments> bigIntegerCases() {
        return Stream.of(
                Arguments.of(new BigInteger("10"), new BigInteger("3"), new BigInteger("30")),
                Arguments.of(new BigInteger("10"), 3, new BigInteger("30")),
                Arguments.of(10, new BigInteger("3"), new BigInteger("30"))
        );
    }

    public static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(-3, 2, BigInteger.valueOf(-6)),
                Arguments.of(3, -2, BigInteger.valueOf(-6)),
                Arguments.of(3, 2, BigInteger.valueOf(6)),
                Arguments.of(-3, -2, BigInteger.valueOf(6))

        );
    }

    public static Stream<Arguments> floatDoubleCases() {
        return Stream.of(
                Arguments.of(1.5f, 2f, new BigDecimal("3.00")),
                Arguments.of(1.5, 2.0, new BigDecimal("3.00")),
                Arguments.of(0.1f, 0.2f, new BigDecimal("0.0200000005960464524408921021677088")),
                Arguments.of(1.25f, 4, new BigDecimal("5.000"))
        );
    }

}
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class DivideTemplateFunctionTest {

    @InjectMocks
    private DivideTemplateFunction function;

    @Test
    void checkNamespace() {
        assertEquals("math", function.getNamespace());
    }

    @Test
    void checkName() {
        assertEquals("div", function.getName());
    }

    @ParameterizedTest
    @MethodSource("cases")
    @MethodSource("bigIntegerCases")
    void divideAsArgs(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("floatDoubleCases")
    @MethodSource("bigDecimalCases")
    void divideFloatAsArgs(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, right, RoundingMode.HALF_UP);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("cases")
    @MethodSource("bigIntegerCases")
    void divideAsPipe(Number left, Number right, Number expected) {
        var args = List.<Object>of(left);
        var result = function.invoke(args, right);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("floatDoubleCases")
    @MethodSource("bigDecimalCases")
    void divideFloatAsPipe(Number left, Number right, Number expected) {
        var args = List.<Object>of(left, RoundingMode.HALF_UP);
        var result = function.invoke(args, right);
        assertEquals(expected, result);
    }

    @Test
    void divideNullValue() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(5);
        var result = function.invoke(args);
        assertNull(result);
    }

    @Test
    void divideNullPipe() {
        var args = List.<Object>of(10);
        var result = function.invoke(args, null);
        assertNull(result);
    }

    @Test
    void invalidArgsType() {
        var args = List.<Object>of("x", 1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:div: not a number passed", exception.getMessage());
    }

    @Test
    void invalidPipeArgType() {
        var args = List.<Object>of(1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "bad"));
        assertEquals("math:div: not a number passed", exception.getMessage());
    }

    @Test
    void tooFewArgsInInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:div: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void tooMuchArgsInInvoke() {
        var args = List.<Object>of("x", "y", "z", "w");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:div: at most 3 arguments required", exception.getMessage());
    }

    @Test
    void tooFewArgsInPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 5));
        assertEquals("math:div: at least 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsInPipeInvoke() {
        var args = List.<Object>of("x", "y", "z");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 5));
        assertEquals("math:div: at most 2 arguments required", exception.getMessage());
    }

    @Test
    void divideByZeroBigInteger() {
        var args = List.<Object>of(BigInteger.TEN, BigInteger.ZERO);
        assertThrows(ArithmeticException.class, () -> function.invoke(args));
    }

    @Test
    void divideByZeroBigDecimal() {
        var args = List.<Object>of(new BigDecimal("1.0"), new BigDecimal("0.0"), RoundingMode.HALF_UP);
        assertThrows(ArithmeticException.class, () -> function.invoke(args));
    }

    @Test
    void divideByZeroFloatDouble() {
        var args = List.<Object>of(1.0, 0.0, RoundingMode.HALF_UP);
        assertThrows(ArithmeticException.class, () -> function.invoke(args));
    }

    @Test
    void divideUsingStringMode() {
        var args = List.<Object>of(
                new BigDecimal("1").setScale(10, RoundingMode.HALF_UP),
                new BigDecimal("3"),
                "HALF_UP"
        );
        var result = function.invoke(args);
        assertEquals(new BigDecimal("0.3333333333"), result);
    }

    public static Stream<Arguments> bigDecimalCases() {
        return Stream.of(
                Arguments.of(new BigDecimal("1.50"), new BigDecimal("2.00"), new BigDecimal("0.75")),
                Arguments.of(new BigDecimal("1.00"), 4, new BigDecimal("0.25")),
                Arguments.of(1, new BigDecimal("4.00"), new BigDecimal("0.25")),
                Arguments.of(
                        new BigDecimal("1").setScale(10, RoundingMode.HALF_UP),
                        new BigDecimal("3"),
                        new BigDecimal("0.3333333333")
                )
        );
    }

    public static Stream<Arguments> bigIntegerCases() {
        return Stream.of(
                Arguments.of(new BigInteger("10"), new BigInteger("2"), new BigInteger("5")),
                Arguments.of(new BigInteger("10"), 3, new BigInteger("3")),
                Arguments.of(10, new BigInteger("3"), new BigInteger("3"))
        );
    }

    public static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(6, 2, BigInteger.valueOf(3)),
                Arguments.of(9, -3, BigInteger.valueOf(-3)),
                Arguments.of(-9, -3, BigInteger.valueOf(3)),
                Arguments.of(10, 3, BigInteger.valueOf(3))
        );
    }

    public static Stream<Arguments> floatDoubleCases() {
        return Stream.of(
                Arguments.of(1.5f, 2f, new BigDecimal("0.8")),
                Arguments.of(3.0, 2.0, new BigDecimal("1.5")),
                Arguments.of(1.0f, 3.0f, new BigDecimal("0.3"))
        );
    }
}

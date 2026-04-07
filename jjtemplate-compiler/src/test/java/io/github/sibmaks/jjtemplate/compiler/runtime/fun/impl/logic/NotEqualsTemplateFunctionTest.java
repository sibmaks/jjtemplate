package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

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
class NotEqualsTemplateFunctionTest {
    @InjectMocks
    private NotEqualsTemplateFunction function;


    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("neq", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("neq: 2 arguments required", exception.getMessage());
    }

    @Test
    void noArgsOnPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("neq: 1 argument required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passAsArguments(Object left, Object right, boolean expected) {
        var args = new ArrayList<>();
        args.add(left);
        args.add(right);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passWithPipe(Object left, Object right, boolean expected) {
        var args = new ArrayList<>();
        args.add(left);
        var actual = function.invoke(args, right);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> cmpCases() {
        var object = new Object();
        return Stream.of(
                Arguments.of(null, null, false),
                Arguments.of("text", null, true),
                Arguments.of(null, "text", true),
                Arguments.of("text", "text", false),
                Arguments.of(42, 42, false),
                Arguments.of(1, BigInteger.ONE, false),
                Arguments.of(BigInteger.ONE, 1, false),
                Arguments.of(1L, BigInteger.ONE, false),
                Arguments.of(Integer.MIN_VALUE, Integer.MIN_VALUE, false),
                Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE, false),
                Arguments.of(Short.MIN_VALUE, Short.MIN_VALUE, false),
                Arguments.of(Short.MAX_VALUE, Short.MAX_VALUE, false),
                Arguments.of(Byte.MAX_VALUE, Byte.MAX_VALUE, false),
                Arguments.of(Byte.MIN_VALUE, Byte.MIN_VALUE, false),
                Arguments.of(3.1415, 3.1415, false),
                Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, false),
                Arguments.of(1, object, true),
                Arguments.of(object, 1, true),
                Arguments.of(object, object, false),
                Arguments.of(object, new Object(), true),
                Arguments.of(new BigDecimal("1.0"), new BigDecimal("1.000"), false),
                Arguments.of(new BigDecimal("1.0"), new BigDecimal("1.0001"), true)
        );
    }
}

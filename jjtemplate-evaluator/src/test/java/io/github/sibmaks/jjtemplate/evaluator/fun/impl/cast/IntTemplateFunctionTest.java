package io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast;

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
class IntTemplateFunctionTest {
    @InjectMocks
    private IntTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("cast", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("int", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("cast:int: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:int: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(1, 2);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:int: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("justToIntCases")
    void justToIntArg(Object input, BigInteger expected) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("justToIntCases")
    void justToIntPipe(Object input, BigInteger expected) {
        var actual = function.invoke(List.of(), input);
        assertEquals(expected, actual);
    }

    @Test
    void invalidStringToIntArg() {
        var args = List.<Object>of("not_a_number");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertTrue(exception.getMessage().startsWith("cast:int: cannot convert: not_a_number"));
    }

    @Test
    void invalidStringToIntPipe() {
        var args = List.of();
        var pipe = "abc";
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, pipe));
        assertTrue(exception.getMessage().startsWith("cast:int: cannot convert: abc"));
    }

    @Test
    void unsupportedTypeArg() {
        var args = List.of(new Object());
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertTrue(exception.getMessage().startsWith("cast:int: cannot convert: java.lang.Object"));
    }

    @Test
    void unsupportedTypePipe() {
        var args = List.of();
        var pipe = new Object();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, pipe));
        assertTrue(exception.getMessage().startsWith("cast:int: cannot convert: java.lang.Object"));
    }

    public static Stream<Arguments> justToIntCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(BigInteger.valueOf(123), BigInteger.valueOf(123)),
                Arguments.of(new BigDecimal("123.456"), BigInteger.valueOf(123)),
                Arguments.of(42, BigInteger.valueOf(42)),
                Arguments.of(42L, BigInteger.valueOf(42)),
                Arguments.of("123456", new BigInteger("123456"))
        );
    }

}
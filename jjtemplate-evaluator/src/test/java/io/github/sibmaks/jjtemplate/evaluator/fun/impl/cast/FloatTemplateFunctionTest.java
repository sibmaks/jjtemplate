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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class FloatTemplateFunctionTest {
    @InjectMocks
    private FloatTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("cast", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("float", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("cast:float: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:float: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(1, 2);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:float: too much arguments passed", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("justToFloatCases")
    void justToFloatArg(Object input, BigDecimal expected) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("justToFloatCases")
    void justToFloatPipe(Object input, BigDecimal expected) {
        var actual = function.invoke(List.of(), input);
        assertEquals(expected, actual);
    }

    @Test
    void invalidStringToFloatArg() {
        var args = List.<Object>of("not_a_number");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("cast:float: cannot convert: not_a_number", exception.getMessage());
    }

    @Test
    void invalidStringToFloatPipe() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "oops"));
        assertEquals("cast:float: cannot convert: oops", exception.getMessage());
    }

    @Test
    void unsupportedTypeArg() {
        var args = List.of(new Object());
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        var message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.startsWith("cast:float: cannot convert: java.lang.Object@"));
    }

    @Test
    void unsupportedTypePipe() {
        var args = List.of();
        var pipe = new Object();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, pipe));
        var message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.startsWith("cast:float: cannot convert: java.lang.Object@"));
    }

    public static Stream<Arguments> justToFloatCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(new BigDecimal("123.456"), new BigDecimal("123.456")),
                Arguments.of(42L, BigDecimal.valueOf(42L)),
                Arguments.of(42, BigDecimal.valueOf(42.0)),
                Arguments.of(3f, BigDecimal.valueOf(3f)),
                Arguments.of("12.34", new BigDecimal("12.34"))
        );
    }
}
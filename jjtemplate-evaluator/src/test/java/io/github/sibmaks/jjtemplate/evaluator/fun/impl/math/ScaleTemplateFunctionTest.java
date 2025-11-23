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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ScaleTemplateFunctionTest {
    @InjectMocks
    private ScaleTemplateFunction function;


    public static Stream<Arguments> scaleCases() {
        return Stream.of(
                Arguments.of(null, 0, "HALF_DOWN", null),
                Arguments.of(42, 0, "HALF_DOWN", new BigDecimal("42")),
                Arguments.of(42, 0, RoundingMode.HALF_DOWN, new BigDecimal("42")),

                Arguments.of(new BigDecimal("3.1415"), 2, "HALF_DOWN", new BigDecimal("3.14")),
                Arguments.of(new BigDecimal("3.1415"), 2, RoundingMode.HALF_DOWN, new BigDecimal("3.14")),

                Arguments.of(new BigDecimal("3.1415"), 4, "HALF_DOWN", new BigDecimal("3.1415")),
                Arguments.of(new BigDecimal("3.1415"), 4, RoundingMode.HALF_DOWN, new BigDecimal("3.1415"))
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("scale", actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("math:scale: 2 arguments required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:scale: 3 arguments required", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:scale: 3 arguments required", exception.getMessage());
    }

    @Test
    void notANumber() {
        var item = UUID.randomUUID().toString();
        var args = List.<Object>of(item, 2, "DOWN");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("math:scale: not a number passed: " + item, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("scaleCases")
    void scale(Object input, int scale, Object roundingMode, Object expected) {
        var args = new ArrayList<>();
        args.add(input);
        args.add(scale);
        args.add(roundingMode);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("scaleCases")
    void scaleAsPipe(Object input, int scale, Object roundingMode, Object expected) {
        var args = new ArrayList<>();
        args.add(scale);
        args.add(roundingMode);
        var actual = function.invoke(args, input);
        assertEquals(expected, actual);
    }

}
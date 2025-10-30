package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class FormatStringTemplateFunctionTest {
    private final FormatStringTemplateFunction function = new FormatStringTemplateFunction(Locale.US);

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("format", actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("format: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void noArgsOnPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("format: at least 1 argument required", exception.getMessage());
    }

    @Test
    void formatFromArguments() {
        var format = "%s-%d-%b";
        var string = UUID.randomUUID().toString();
        var number = UUID.randomUUID().hashCode();
        var bool = UUID.randomUUID().hashCode() % 2 == 0;
        var args = List.<Object>of(
                format,
                string,
                number,
                bool
        );
        var actual = function.invoke(args);
        assertEquals(String.format(format, string, number, bool), actual);
    }

    @Test
    void formatFromPipe() {
        var format = "%s-%d-%b";
        var string = UUID.randomUUID().toString();
        var number = UUID.randomUUID().hashCode();
        var bool = UUID.randomUUID().hashCode() % 2 == 0;
        var args = List.<Object>of(
                format,
                string,
                number
        );
        var actual = function.invoke(args, bool);
        assertEquals(String.format(format, string, number, bool), actual);
    }

    @Test
    void formatBigDecimal() {
        var format = "%.2f";
        var random = new BigDecimal(Math.random());
        var args = List.<Object>of(
                format,
                random
        );
        var actual = function.invoke(args);
        assertFalse(actual.isEmpty());
        var excepted = random.setScale(2, RoundingMode.HALF_UP);
        assertEquals(excepted.toPlainString(), actual);
    }


}
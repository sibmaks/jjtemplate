package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

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
@ExtendWith(MockitoExtension.class)
class StringFormatTemplateFunctionTest {
    @InjectMocks
    private StringFormatTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("format", actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:format: at least 1 argument required", exception.getMessage());
    }

    @Test
    void noArgsOnPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("string:format: at least 1 argument required", exception.getMessage());
    }

    @Test
    void localeOnlyOnInvoke() {
        var args = List.<Object>of(Locale.FRANCE);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:format: at least 2 arguments required", exception.getMessage());
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
    void formatWithLocaleFirst() {
        var format = "%s-%d-%b";
        var string = "LocaleFirst";
        var number = 42;
        var bool = true;
        var args = List.<Object>of(
                Locale.US,
                format,
                string,
                number,
                bool
        );
        var actual = function.invoke(args);
        assertEquals(String.format(Locale.US, format, string, number, bool), actual);
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
    void formatFromPipeWithLocale() {
        var format = "%s-%d-%b";
        var string = "PipeLocale";
        var number = 7;
        var bool = false;
        var args = List.<Object>of(
                Locale.CANADA,
                format,
                string,
                number
        );
        var actual = function.invoke(args, bool);
        assertEquals(String.format(Locale.CANADA, format, string, number, bool), actual);
    }

    @Test
    void formatBigDecimal() {
        var format = "%.2f";
        var random = BigDecimal.valueOf(Math.random());
        var args = List.<Object>of(
                format,
                random
        );
        var actual = function.invoke(args);
        assertFalse(actual.isEmpty());
        var expected = random.setScale(2, RoundingMode.HALF_UP);
        assertEquals(String.format(format, expected), actual);
    }

    @Test
    void formatBigDecimalWithLocale() {
        var format = "%.2f";
        var random = new BigDecimal("3.14159");
        var args = List.<Object>of(
                Locale.FRANCE,
                format,
                random
        );
        var actual = function.invoke(args);
        assertTrue(actual.contains("3,14") || actual.contains("3.14"));
    }


}
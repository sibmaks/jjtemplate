package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class FormatDateTemplateFunctionTest {
    @InjectMocks
    private FormatDateTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("formatDate", actual);
    }

    @Test
    void validInvokeArgsWithTemporalAccessor() {
        var format = "yyyy-MM-dd HH:mm";
        var date = LocalDateTime.of(2025, 11, 5, 8, 45);
        var expected = DateTimeFormatter.ofPattern(format).format(date);

        var args = List.<Object>of(format, date);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokePipeWithTemporalAccessor() {
        var format = "dd/MM/yyyy";
        var date = LocalDateTime.of(2025, 11, 5, 0, 0);
        var expected = DateTimeFormatter.ofPattern(format).format(date);

        var args = List.<Object>of(format);
        var actual = function.invoke(args, date);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokeArgsWithDate() {
        var format = "yyyy-MM-dd";
        var date = new Date(1730784000000L); // 2024-11-05 UTC
        var expected = new SimpleDateFormat(format).format(date);

        var args = List.<Object>of(format, date);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokePipeWithDate() {
        var format = "dd-MM-yyyy";
        var date = new Date(1730784000000L);
        var expected = new SimpleDateFormat(format).format(date);

        var args = List.<Object>of(format);
        var actual = function.invoke(args, date);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokeArgsWithLocaleAndDate() {
        var format = "dd MMM yyyy";
        var locale = Locale.FRANCE;
        var date = new Date(1730784000000L);
        var expected = new SimpleDateFormat(format, locale).format(date);

        var args = List.<Object>of(locale, format, date);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokePipeWithLocaleAndDate() {
        var format = "EEEE dd MMMM yyyy";
        var locale = Locale.GERMANY;
        var date = new Date(1730784000000L);
        var expected = new SimpleDateFormat(format, locale).format(date);

        var args = List.<Object>of(locale, format);
        var actual = function.invoke(args, date);
        assertEquals(expected, actual);
    }

    @Test
    void invalidTypeInInvokeArgs() {
        var args = List.<Object>of("yyyy-MM-dd", 42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertTrue(exception.getMessage().startsWith("Cannot convert 42"));
    }

    @Test
    void invalidTypeInInvokePipe() {
        var args = List.<Object>of("yyyy-MM-dd");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 42));
        assertTrue(exception.getMessage().startsWith("Cannot convert 42"));
    }

    @Test
    void wrongArgsCountInInvoke() {
        var args = List.<Object>of("yyyy-MM-dd");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("formatDate: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void wrongArgsCountInInvokePipe() {
        var args = List.of();
        var date = new Date();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, date));
        assertEquals("formatDate: at least 1 argument required", exception.getMessage());
    }

    @Test
    void wrongArgsCountInInvokeLocale() {
        var args = List.<Object>of(Locale.US, "test");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("formatDate: at least 3 arguments required", exception.getMessage());
    }

    @Test
    void wrongArgsCountInInvokePipeLocale() {
        var args = List.<Object>of(Locale.US);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, new Date()));
        assertEquals("formatDate: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void tooMuchArgsInInvokePipe() {
        var args = List.<Object>of("test", 42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, new Date()));
        assertEquals("formatDate: 1 argument required", exception.getMessage());
    }
}

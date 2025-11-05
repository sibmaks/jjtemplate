package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ParseDateTemplateFunctionTest {
    @InjectMocks
    private ParseDateTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("parseDate", actual);
    }

    @Test
    void validInvokeArgs() {
        var format = "yyyy-MM-dd";
        var dateString = "2025-11-05";
        var expected = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));

        var args = List.<Object>of(format, dateString);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokePipe() {
        var format = "dd/MM/yyyy";
        var dateString = "05/11/2025";
        var expected = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));

        var args = List.<Object>of(format);
        var actual = function.invoke(args, dateString);
        assertEquals(expected, actual);
    }

    @Test
    void wrongArgsCountInInvokePipe() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "05/11/2025"));
        assertEquals("parseDate: 1 argument required", exception.getMessage());
    }

    @Test
    void wrongArgsCountInInvoke() {
        var args = List.<Object>of("yyyy-MM-dd");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("parseDate: 2 arguments required", exception.getMessage());
    }

    @Test
    void invalidDateStringInvoke() {
        var args = List.<Object>of("yyyy-MM-dd", "invalid-date");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("Invalid date string: invalid-date", exception.getMessage());
        assertInstanceOf(java.time.format.DateTimeParseException.class, exception.getCause());
    }

    @Test
    void invalidDateStringPipe() {
        var args = List.<Object>of("yyyy-MM-dd");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "not-a-date"));
        assertEquals("Invalid date string: not-a-date", exception.getMessage());
        assertInstanceOf(java.time.format.DateTimeParseException.class, exception.getCause());
    }
}
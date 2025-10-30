package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ParseDateTimeTemplateFunctionTest {
    @InjectMocks
    private ParseDateTimeTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("parseDateTime", actual);
    }

    @Test
    void validInvokeArgs() {
        var format = "yyyy-MM-dd HH:mm:ss";
        var dateString = "2024-05-12 13:45:30";
        var expected = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format));

        var args = List.<Object>of(format, dateString);
        var actual = function.invoke(args);
        assertEquals(expected, actual);
    }

    @Test
    void validInvokePipe() {
        var format = "yyyy-MM-dd HH:mm";
        var dateString = "2025-11-05 08:30";
        var expected = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format));

        var args = List.<Object>of(format);
        var actual = function.invoke(args, dateString);
        assertEquals(expected, actual);
    }

    @Test
    void wrongArgsCountInInvokePipe() {
        var args = List.of(); // empty, should fail
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "2025-11-05 08:30"));
        assertEquals("parseDateTime: 1 argument required", exception.getMessage());
    }

    @Test
    void wrongArgsCountInInvoke() {
        var args = List.<Object>of("yyyy-MM-dd"); // only one arg, need two
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("parseDateTime: 2 arguments required", exception.getMessage());
    }

    @Test
    void invalidDateFormatInvoke() {
        var args = List.<Object>of("yyyy-MM-dd HH:mm:ss", "not-a-date");
        var exception = assertThrows(Exception.class, () -> function.invoke(args));
        assertInstanceOf(TemplateEvalException.class, exception);
    }

    @Test
    void invalidDateFormatPipe() {
        var args = List.<Object>of("yyyy-MM-dd HH:mm:ss");
        assertThrows(Exception.class, () -> function.invoke(args, "bad-date"));
    }
}
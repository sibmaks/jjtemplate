package io.github.sibmaks.jjtemplate.evaluator.fun.impl.datetime;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class DateTimeParseTemplateFunctionTest {
    @InjectMocks
    private DateTimeParseTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("datetime", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("parse", actual);
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
    void invokeArgsWithNull() {
        var args = new ArrayList<>();
        args.add("yyyy-MM-dd");
        args.add(null);
        var actual = function.invoke(args);
        assertNull(actual);
    }

    @Test
    void invokePipeWithNull() {
        var args = new ArrayList<>();
        args.add("yyyy-MM-dd");
        var actual = function.invoke(args, null);
        assertNull(actual);
    }

    @Test
    void wrongArgsCountInInvokePipe() {
        var args = List.of(); // empty, should fail
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "2025-11-05 08:30"));
        assertEquals("datetime:parse: 1 argument required", exception.getMessage());
    }

    @Test
    void wrongArgsCountInInvoke() {
        var args = List.<Object>of("yyyy-MM-dd"); // only one arg, need two
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("datetime:parse: 2 arguments required", exception.getMessage());
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
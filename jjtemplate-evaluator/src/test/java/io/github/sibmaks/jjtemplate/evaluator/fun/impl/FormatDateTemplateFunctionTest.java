package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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
    void formatLocalDateFromArguments() {
        var format = "dd.MM.yyyy";
        var localDate = LocalDate.now();
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatLocalDateFromPipe() {
        var format = "dd.MM.yyyy";
        var localDate = LocalDate.now();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatLocalDateTimeFromArguments() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var localDate = LocalDateTime.now();
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatLocalDateTimeFromPipe() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var localDate = LocalDateTime.now();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatDateFromArguments() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var localDate = new Date();
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());

        var formatter = new SimpleDateFormat(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatDateFromPipe() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var localDate = new Date();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = new SimpleDateFormat(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatDateFromArgumentsToDateOnly() {
        var format = "dd.MM.yyyy";
        var localDate = new Date();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = new SimpleDateFormat(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatDateFromPipeToDateOnly() {
        var format = "dd.MM.yyyy";
        var localDate = new Date();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = new SimpleDateFormat(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void onInvalidDateType() {
        var format = "dd.MM.yyyy";
        var args = List.of(
                ExpressionValue.of(format)
        );
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, ExpressionValue.of(String.class))
        );
        assertEquals("Cannot convert " + String.class + " to TemporalAccessor", exception.getMessage());
    }

}
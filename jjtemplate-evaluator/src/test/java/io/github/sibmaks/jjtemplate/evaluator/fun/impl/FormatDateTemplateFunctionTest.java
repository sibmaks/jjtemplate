package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author sibmaks
 */
class FormatDateTemplateFunctionTest {
    private final FormatDateTemplateFunction function = new FormatDateTemplateFunction();

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

}
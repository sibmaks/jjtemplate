package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author sibmaks
 */
class ParseDateTemplateFunctionTest {
    private final TemplateFunction function = new ParseDateTemplateFunction();

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("parseDate", actual);
    }

    @Test
    void formatLocalDateFromArguments() {
        var format = "dd.MM.yyyy";
        var localDateRaw = LocalDate.now();
        var localDate = localDateRaw.format(DateTimeFormatter.ofPattern(format));
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(localDateRaw, actual.getValue());
    }

    @Test
    void formatLocalDateFromPipe() {
        var format = "dd.MM.yyyy";
        var localDateRaw = LocalDate.now();
        var localDate = localDateRaw.format(DateTimeFormatter.ofPattern(format));
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());
        assertEquals(localDateRaw, actual.getValue());
    }

}
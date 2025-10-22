package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author sibmaks
 */
class ParseDateTimeTemplateFunctionTest {
    private final TemplateFunction function = new ParseDateTimeTemplateFunction();

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("parseDateTime", actual);
    }

    @Test
    void formatLocalDateFromArguments() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var raw = LocalDateTime.now().withNano(0);
        var localDate = raw.format(DateTimeFormatter.ofPattern(format));
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(raw, actual.getValue());
    }

    @Test
    void formatLocalDateFromPipe() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var raw = LocalDateTime.now().withNano(0);
        var localDate = raw.format(DateTimeFormatter.ofPattern(format));
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());
        assertEquals(raw, actual.getValue());
    }

}
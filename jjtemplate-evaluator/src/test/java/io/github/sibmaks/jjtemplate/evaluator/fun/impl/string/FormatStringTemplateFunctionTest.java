package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author sibmaks
 */
class FormatStringTemplateFunctionTest {
    private final TemplateFunction function = new FormatStringTemplateFunction();

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("format", actual);
    }

    @Test
    void formatFromArguments() {
        var format = "%s-%d-%b";
        var string = UUID.randomUUID().toString();
        var number = UUID.randomUUID().hashCode();
        var bool = UUID.randomUUID().hashCode() % 2 == 0;
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(string),
                ExpressionValue.of(number),
                ExpressionValue.of(bool)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(String.format(format, string, number, bool), actual.getValue());
    }

    @Test
    void formatFromPipe() {
        var format = "%s-%d-%b";
        var string = UUID.randomUUID().toString();
        var number = UUID.randomUUID().hashCode();
        var bool = UUID.randomUUID().hashCode() % 2 == 0;
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(string),
                ExpressionValue.of(number)
        );
        var actual = function.invoke(args, ExpressionValue.of(bool));
        assertFalse(actual.isEmpty());
        assertEquals(String.format(format, string, number, bool), actual.getValue());
    }

    @Test
    void formatBigDecimal() {
        var format = "%.2f";
        var random = new BigDecimal(Math.random());
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(random)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        var excepted = random.setScale(2, RoundingMode.HALF_UP);
        assertEquals(excepted.toPlainString(), actual.getValue());
    }

}
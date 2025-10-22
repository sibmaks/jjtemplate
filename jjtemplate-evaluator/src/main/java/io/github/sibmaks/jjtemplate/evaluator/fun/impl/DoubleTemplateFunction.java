package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author sibmaks
 */
public class DoubleTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var argument = first(args, pipeArg);
        if (argument.isEmpty()) {
            return argument;
        }
        var value = argument.getValue();
        if (value == null) {
            return ExpressionValue.empty();
        }
        if (value instanceof BigDecimal) {
            return argument;
        }
        if (value instanceof Number) {
            var doubleValue = ((Number) value).doubleValue();
            return ExpressionValue.of(BigDecimal.valueOf(doubleValue));
        }
        if (value instanceof String) {
            try {
                var bigDecimal = new BigDecimal((String) value);
                return ExpressionValue.of(bigDecimal);
            } catch (Exception ignored) {
            }
        }
        throw new TemplateEvalException("double: cannot convert: " + value);
    }

    @Override
    public String getName() {
        return "double";
    }
}

package io.github.sibmaks.jjtemplate.evaulator.fun.impl;

import io.github.sibmaks.jjtemplate.evaulator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;

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
            return null;
        }
        if (value instanceof Double) {
            return argument;
        }
        if (value instanceof Number) {
            var intValue = ((Number) value).doubleValue();
            return ExpressionValue.of(intValue);
        }
        if (value instanceof String) {
            try {
                var doubleValue = Double.valueOf((String) value);
                return ExpressionValue.of(doubleValue);
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

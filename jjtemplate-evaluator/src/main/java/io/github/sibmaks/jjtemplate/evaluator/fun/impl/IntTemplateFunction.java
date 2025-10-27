package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class IntTemplateFunction implements TemplateFunction {
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
        if (value instanceof Integer) {
            return argument;
        }
        if (value instanceof Number) {
            var intValue = ((Number) value).intValue();
            return ExpressionValue.of(intValue);
        }
        if (value instanceof String) {
            try {
                var intValue = Integer.valueOf((String) value);
                return ExpressionValue.of(intValue);
            } catch (Exception e) {
                throw new TemplateEvalException("int: cannot convert: " + value, e);
            }
        }
        throw new TemplateEvalException("int: cannot convert: " + value);
    }

    @Override
    public String getName() {
        return "int";
    }
}

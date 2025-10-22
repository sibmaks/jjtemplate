package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author sibmaks
 */
public class NegTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var argument = first(args, pipeArg);
        if (argument.isEmpty()) {
            throw new TemplateEvalException("neg: required 1 argument");
        }
        var value = argument.getValue();
        if (value == null) {
            return ExpressionValue.empty();
        }
        if (value instanceof Integer) {
            return ExpressionValue.of(-(Integer) value);
        }
        if (value instanceof BigDecimal) {
            return ExpressionValue.of(((BigDecimal) value).negate());
        }
        if (value instanceof Number) {
            var doubleValue = ((Number) value).doubleValue();
            return ExpressionValue.of(-doubleValue);
        }
        throw new TemplateEvalException("neg: cannot execute: " + value);
    }

    @Override
    public String getName() {
        return "neg";
    }
}

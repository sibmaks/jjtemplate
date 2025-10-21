package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class BooleanTemplateFunction implements TemplateFunction {
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
        if (value instanceof Boolean) {
            return argument;
        }
        if (value instanceof String) {
            try {
                var strValue = (String) value;
                if("true".equals(strValue)) {
                    return ExpressionValue.of(true);
                }
                if("false".equals(strValue)) {
                    return ExpressionValue.of(false);
                }
            } catch (Exception ignored) {
            }
        }
        throw new TemplateEvalException("boolean: cannot convert: " + value);
    }

    @Override
    public String getName() {
        return "boolean";
    }
}

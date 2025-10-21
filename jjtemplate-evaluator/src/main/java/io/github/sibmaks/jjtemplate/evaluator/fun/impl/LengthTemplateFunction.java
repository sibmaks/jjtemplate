package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sibmaks
 */
public class LengthTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var argument = first(args, pipeArg);
        if (argument.isEmpty()) {
            return ExpressionValue.of(0);
        }
        var value = argument.getValue();
        if (value instanceof CharSequence) {
            return ExpressionValue.of(((CharSequence) value).length());
        }
        if (value instanceof Collection) {
            return ExpressionValue.of(((Collection<?>) value).size());
        }
        if (value instanceof Map) {
            return ExpressionValue.of(((Map<?, ?>) value).size());
        }
        if (value.getClass().isArray()) {
            return ExpressionValue.of(Array.getLength(value));
        }
        throw new TemplateEvalException("len: unsupported type: " + value.getClass());
    }

    @Override
    public String getName() {
        return "len";
    }
}

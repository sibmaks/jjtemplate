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
public class EmptyTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var argument = first(args, pipeArg);
        if (argument.isEmpty()) {
            return ExpressionValue.of(true);
        }
        var value = argument.getValue();
        if (value instanceof CharSequence) {
            return ExpressionValue.of(((CharSequence) value).length() == 0);
        }
        if (value instanceof Collection) {
            return ExpressionValue.of(((Collection<?>) value).isEmpty());
        }
        if (value instanceof Map) {
            return ExpressionValue.of(((Map<?, ?>) value).isEmpty());
        }
        if (value.getClass().isArray()) {
            return ExpressionValue.of(Array.getLength(value) == 0);
        }
        throw new TemplateEvalException("empty: unsupported type: " + value.getClass());
    }

    @Override
    public String getName() {
        return "empty";
    }
}

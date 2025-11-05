package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class EmptyTemplateFunction implements TemplateFunction<Boolean> {

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        throw new TemplateEvalException("empty: unsupported type: " + value.getClass());
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("empty: too much arguments passed");
        }
        return isEmpty(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("empty: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("empty: too much arguments passed");
        }
        return isEmpty(args.get(0));
    }

    @Override
    public String getName() {
        return "empty";
    }
}

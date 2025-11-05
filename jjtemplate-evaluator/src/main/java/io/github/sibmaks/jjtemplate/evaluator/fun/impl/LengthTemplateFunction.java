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
public class LengthTemplateFunction implements TemplateFunction<Integer> {

    private static int getLength(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).size();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        throw new TemplateEvalException("len: unsupported type: " + value.getClass());
    }

    @Override
    public Integer invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("len: too much arguments passed");
        }
        return getLength(pipeArg);
    }

    @Override
    public Integer invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("len: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("len: too much arguments passed");
        }
        return getLength(args.get(0));
    }

    @Override
    public String getName() {
        return "len";
    }
}

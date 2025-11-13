package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that returns a fallback value when the input is {@code null}.
 *
 * <p>In pipe form, returns the first argument if the piped value is {@code null},
 * otherwise returns the piped value. In direct form, returns the second argument
 * when the first is {@code null}.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class DefaultTemplateFunction implements TemplateFunction<Object> {
    @Override
    public Object invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (pipeArg == null) {
            return args.get(0);
        }
        return pipeArg;
    }

    @Override
    public Object invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var value = args.get(0);
        if (value == null) {
            return args.get(1);
        }
        return value;
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "default";
    }
}

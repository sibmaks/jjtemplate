package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class DefaultTemplateFunction implements TemplateFunction<Object> {
    @Override
    public Object invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("default: 1 argument required");
        }
        if (pipeArg == null) {
            return args.get(0);
        }
        return pipeArg;
    }

    @Override
    public Object invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("default: 2 arguments required");
        }
        var value =  args.get(0);
        if(value == null) {
            return args.get(1);
        }
        return value;
    }

    @Override
    public String getName() {
        return "default";
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Objects;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class NotEqualsTemplateFunction implements TemplateFunction<Boolean> {

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw new TemplateEvalException("neq: 1 argument required");
        }
        return !Objects.equals(args.get(0), pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("neq: 2 arguments required");
        }
        return !Objects.equals(args.get(0), args.get(1));
    }

    @Override
    public String getName() {
        return "neq";
    }
}

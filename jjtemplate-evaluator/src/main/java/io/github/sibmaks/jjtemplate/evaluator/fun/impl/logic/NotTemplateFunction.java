package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class NotTemplateFunction implements TemplateFunction<Boolean> {

    private static boolean not(Object value) {
        if (!(value instanceof Boolean)) {
            throw new TemplateEvalException("not: argument must be a boolean");
        }
        var x = (boolean) value;
        return !x;
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("not: too much arguments passed");
        }
        return not(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 1) {
            throw new TemplateEvalException("not: 1 argument required");
        }
        return not(args.get(0));
    }

    @Override
    public String getName() {
        return "not";
    }
}

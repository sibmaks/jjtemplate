package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class AndTemplateFunction implements TemplateFunction<Boolean> {

    private static boolean and(Object left, Object right) {
        if (!(left instanceof Boolean)) {
            throw new TemplateEvalException("and: All arguments must be a boolean");
        }
        if (!(right instanceof Boolean)) {
            throw new TemplateEvalException("and: All arguments must be a boolean");
        }
        var x = (boolean) left;
        var y = (boolean) right;
        return x && y;
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw new TemplateEvalException("and: 1 argument required");
        }
        return and(args.get(0), pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("and: 2 arguments required");
        }
        return and(args.get(0), args.get(1));
    }

    @Override
    public String getName() {
        return "and";
    }
}

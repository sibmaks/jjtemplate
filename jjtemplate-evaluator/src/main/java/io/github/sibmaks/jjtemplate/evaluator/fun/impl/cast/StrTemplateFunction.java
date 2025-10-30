package io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public final class StrTemplateFunction implements TemplateFunction<String> {
    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("str: too much arguments passed");
        }
        if (pipeArg == null) {
            return null;
        }
        return String.valueOf(pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("str: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("str: too much arguments passed");
        }
        var arg = args.get(0);
        if (arg == null) {
            return null;
        }
        return String.valueOf(arg);
    }

    @Override
    public String getName() {
        return "str";
    }
}

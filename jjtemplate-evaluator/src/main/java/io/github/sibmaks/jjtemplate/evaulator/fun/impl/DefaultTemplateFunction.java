package io.github.sibmaks.jjtemplate.evaulator.fun.impl;

import io.github.sibmaks.jjtemplate.evaulator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class DefaultTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.isEmpty()) {
            return pipeArg;
        }
        if (args.size() == 1) {
            return pipeArg != null ? pipeArg : args.get(0);
        }
        if (args.size() == 2) {
            return args.get(0) != null ? args.get(0) : args.get(1);
        }
        throw new TemplateEvalException("default: invalid args");
    }

    @Override
    public String getName() {
        return "default";
    }
}

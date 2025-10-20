package io.github.sibmaks.jjtemplate.evaulator.fun.impl;

import io.github.sibmaks.jjtemplate.evaulator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class OptionalTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.size() == 1) {
            return ((Boolean) args.get(0).getValue()) ? pipeArg : ExpressionValue.empty();
        }
        if (args.size() == 2) {
            return ((Boolean) args.get(0).getValue()) ? args.get(1) : ExpressionValue.empty();
        }
        throw new TemplateEvalException("optional: invalid args");
    }

    @Override
    public String getName() {
        return "optional";
    }
}

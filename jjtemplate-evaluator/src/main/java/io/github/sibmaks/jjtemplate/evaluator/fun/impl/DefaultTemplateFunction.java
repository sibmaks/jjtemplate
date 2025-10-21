package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

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
        var conditionValue = args.get(0);
        if (args.size() == 1) {
            return pipeArg.getValue() != null ? pipeArg : conditionValue;
        }
        if (args.size() == 2) {
            return conditionValue.getValue() != null ? conditionValue : args.get(1);
        }
        throw new TemplateEvalException("default: invalid args");
    }

    @Override
    public String getName() {
        return "default";
    }
}

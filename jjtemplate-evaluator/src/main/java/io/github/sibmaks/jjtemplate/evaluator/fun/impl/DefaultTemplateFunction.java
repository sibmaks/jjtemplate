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
        if (args.size() + (pipeArg.isEmpty() ? 0 : 1) != 2) {
            throw new TemplateEvalException("default: 2 arguments required");
        }
        var conditionValue = args.get(0);
        if (args.size() == 1) {
            return pipeArg.getValue() != null ? pipeArg : conditionValue;
        }
        return conditionValue.getValue() != null ? conditionValue : args.get(1);
    }

    @Override
    public String getName() {
        return "default";
    }
}

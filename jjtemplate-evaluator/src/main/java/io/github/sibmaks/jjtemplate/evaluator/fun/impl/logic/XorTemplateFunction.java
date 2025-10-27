package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class XorTemplateFunction implements TemplateFunction {
    private static ExpressionValue second(List<ExpressionValue> args, ExpressionValue pipeArg) {
        return args.size() > 1 ? args.get(1) : pipeArg;
    }

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.size() + (pipeArg.isEmpty() ? 0 : 1) != 2) {
            throw new TemplateEvalException("xor: 2 arguments required");
        }
        var firstArg = first(args, pipeArg);
        var firstValue = firstArg.getValue();
        if (!(firstValue instanceof Boolean)) {
            throw new TemplateEvalException("xor: All arguments must be a boolean");
        }
        var secondArg = second(args, pipeArg);
        var secondValue = secondArg.getValue();
        if (!(secondValue instanceof Boolean)) {
            throw new TemplateEvalException("xor: All arguments must be a boolean");
        }
        var x = (boolean) firstValue;
        var y = (boolean) secondValue;
        return ExpressionValue.of(x ^ y);
    }

    @Override
    public String getName() {
        return "xor";
    }
}

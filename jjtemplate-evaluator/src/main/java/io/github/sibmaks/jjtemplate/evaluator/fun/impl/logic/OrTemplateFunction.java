package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class OrTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var firstArg = first(args, pipeArg);
        var x = (boolean) firstArg.getValue();
        if(x) {
            return ExpressionValue.of(true);
        }
        var y = (boolean) (args.size() > 1 ? args.get(1).getValue() : pipeArg.getValue());
        return ExpressionValue.of(y);
    }

    @Override
    public String getName() {
        return "or";
    }
}

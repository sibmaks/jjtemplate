package io.github.sibmaks.jjtemplate.evaulator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class OrTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var x = (Boolean) first(args, pipeArg).getValue();
        if(x) {
            return ExpressionValue.of(true);
        }
        var y = (Boolean) (args.size() > 1 ? args.get(1).getValue() : pipeArg.getValue());
        return ExpressionValue.of(y);
    }

    @Override
    public String getName() {
        return "or";
    }
}

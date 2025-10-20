package io.github.sibmaks.jjtemplate.evaulator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class GTCompareTemplateFunction extends CompareTemplateFunction {

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        return ExpressionValue.of(fnCmp(args, pipeArg, 1, false));
    }

    @Override
    public String getName() {
        return "gt";
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class StrTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var argument = first(args, pipeArg);
        if(argument.isEmpty() || argument.getValue() == null) {
            return argument;
        }
        var value = argument.getValue();
        return ExpressionValue.of(String.valueOf(value));
    }

    @Override
    public String getName() {
        return "str";
    }
}

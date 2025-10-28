package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class ListTemplateFunction implements TemplateFunction {

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var all = new ArrayList<>();
        for (var arg : args) {
            all.add(arg.getValue());
        }
        if (!pipeArg.isEmpty()) {
            all.add(pipeArg.getValue());
        }
        return ExpressionValue.of(all);
    }

    @Override
    public String getName() {
        return "list";
    }
}

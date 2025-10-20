package io.github.sibmaks.jjtemplate.evaulator.fun.impl;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sibmaks
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

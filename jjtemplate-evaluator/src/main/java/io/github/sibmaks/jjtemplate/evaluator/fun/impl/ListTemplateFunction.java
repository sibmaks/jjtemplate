package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class ListTemplateFunction implements TemplateFunction<List<?>> {

    @Override
    public List<?> invoke(List<Object> args, Object pipeArg) {
        var out = new ArrayList<>(args.size() + 1);
        out.addAll(args);
        out.add(pipeArg);
        return out;
    }

    @Override
    public List<?> invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("list: at least 1 argument required");
        }
        return List.copyOf(args);
    }

    @Override
    public String getName() {
        return "list";
    }
}

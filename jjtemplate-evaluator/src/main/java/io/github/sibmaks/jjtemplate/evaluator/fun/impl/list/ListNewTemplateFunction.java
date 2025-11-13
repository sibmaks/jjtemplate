package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Template function that creates a new list from the provided arguments.
 *
 * <p>In pipe form, appends the piped value to the end of the list.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public class ListNewTemplateFunction implements TemplateFunction<List<?>> {

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
            throw fail("at least 1 argument required");
        }
        return List.copyOf(args);
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "new";
    }
}

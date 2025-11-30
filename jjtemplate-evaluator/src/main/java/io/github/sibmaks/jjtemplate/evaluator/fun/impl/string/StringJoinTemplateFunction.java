package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringJoinTemplateFunction implements TemplateFunction<String> {
    private static String join(String glue, List<Object> args) {
        return args.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(glue));
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var glue = String.valueOf(args.get(0));
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        all.add(pipeArg);
        return join(glue, all);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        var glue = String.valueOf(args.get(0));
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        return join(glue, all);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "join";
    }
}

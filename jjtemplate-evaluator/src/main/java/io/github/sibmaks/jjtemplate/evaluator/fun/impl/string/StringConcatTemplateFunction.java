package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template function that concatenates multiple values into a single string.
 *
 * <p>The first argument defines the initial string; subsequent values are appended
 * in order. In pipe form, the piped value is appended last.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public class StringConcatTemplateFunction implements TemplateFunction<String> {
    private static String concat(Object first, List<Object> args) {
        var sb = new StringBuilder(String.valueOf(first));
        for (var v : args) {
            sb.append(v);
        }
        return sb.toString();
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        all.add(pipeArg);
        return concat(args.get(0), all);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        return concat(args.get(0), all);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "concat";
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;

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
    private static String concat(List<Object> args) {
        var sb = new StringBuilder();
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
        var all = new ArrayList<>(args);
        all.add(pipeArg);
        return concat(all);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        return concat(args);
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

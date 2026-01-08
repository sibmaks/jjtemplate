package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template function that joins multiple values into a single string.
 * <p>
 * All arguments are converted to strings and concatenated using the configured
 * delimiter, preserving argument order.
 * </p>
 *
 * <p>
 * {@code null} values are converted using {@link String#valueOf(Object)}.
 * </p>
 *
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

    @Override
    public boolean isDynamic() {
        return false;
    }
}

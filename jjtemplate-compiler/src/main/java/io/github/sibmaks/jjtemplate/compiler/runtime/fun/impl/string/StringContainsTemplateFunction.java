package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Template function that checks whether a string contains all specified substrings.
 *
 * <p>Values are converted to strings before comparison. In pipe form, the piped
 * value is used as the target string.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringContainsTemplateFunction implements TemplateFunction<Boolean> {

    private boolean contains(Object value, Set<Object> args) {
        var line = String.valueOf(value);
        for (var item : args) {
            if (!line.contains(item.toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        return contains(pipeArg, new HashSet<>(args));
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        var container = args.get(0);
        var toCheck = args.subList(1, args.size());
        return contains(container, new HashSet<>(toCheck));
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "contains";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

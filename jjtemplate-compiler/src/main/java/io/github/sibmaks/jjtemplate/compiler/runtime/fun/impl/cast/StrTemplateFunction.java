package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.cast;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that converts a value to a string using {@link String#valueOf(Object)}.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class StrTemplateFunction implements TemplateFunction<String> {
    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        if (pipeArg == null) {
            return null;
        }
        return String.valueOf(pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var arg = args.get(0);
        if (arg == null) {
            return null;
        }
        return String.valueOf(arg);
    }

    @Override
    public String getNamespace() {
        return "cast";
    }

    @Override
    public String getName() {
        return "str";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

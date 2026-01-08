package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;
import java.util.Objects;

/**
 * Template function that checks two values for inequality using {@link Objects#equals}.
 *
 * <p>Supports both direct and pipe invocation forms.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class NotEqualsTemplateFunction implements TemplateFunction<Boolean> {

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        return !Objects.equals(args.get(0), pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        return !Objects.equals(args.get(0), args.get(1));
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "neq";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

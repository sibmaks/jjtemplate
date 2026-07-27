package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that performs logical AND on boolean values.
 *
 * <p>Accepts exactly two boolean operands, either via direct call or pipe form.
 * Non-boolean values result in an error.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class AndTemplateFunction implements TemplateFunction<Boolean> {

    private boolean requireBoolean(Object value) {
        if (!(value instanceof Boolean)) {
            throw fail("all arguments must be a boolean");
        }
        return (boolean) value;
    }

    private boolean and(Object left, List<Object> remainingArgs) {
        if (!(left instanceof Boolean)) {
            throw fail("all arguments must be a boolean");
        }
        var x = (boolean) left;
        return x && requireBoolean(remainingArgs.get(0));
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        return and(pipeArg, args);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var left = requireBoolean(args.get(0));
        return left && requireBoolean(args.get(1));
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "and";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public boolean isLazy() {
        return true;
    }
}

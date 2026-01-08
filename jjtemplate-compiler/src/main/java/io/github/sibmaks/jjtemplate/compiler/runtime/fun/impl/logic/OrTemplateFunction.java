package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that performs logical OR on boolean values.
 *
 * <p>Accepts exactly two boolean operands, either via direct call or pipe form.
 * Non-boolean values result in an error.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class OrTemplateFunction implements TemplateFunction<Boolean> {

    private boolean or(Object left, Object right) {
        if (!(left instanceof Boolean)) {
            throw fail("all arguments must be a boolean");
        }
        if (!(right instanceof Boolean)) {
            throw fail("all arguments must be a boolean");
        }
        var x = (boolean) left;
        var y = (boolean) right;
        return x || y;
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        return or(args.get(0), pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        return or(args.get(0), args.get(1));
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "or";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

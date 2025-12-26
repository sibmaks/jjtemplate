package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that performs logical negation on a boolean value.
 *
 * <p>Accepts exactly two boolean operand, either via direct call or pipe form.
 * Non-boolean values result in an error.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class NotTemplateFunction implements TemplateFunction<Boolean> {

    private boolean not(Object value) {
        if (!(value instanceof Boolean)) {
            throw fail("argument must be a boolean");
        }
        var x = (boolean) value;
        return !x;
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return not(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        return not(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "not";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

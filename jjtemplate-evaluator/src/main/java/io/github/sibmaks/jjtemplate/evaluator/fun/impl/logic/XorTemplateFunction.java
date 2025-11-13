package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that performs logical XOR on boolean values.
 *
 * <p>Accepts exactly two boolean operands, either via direct call or pipe form.
 * Non-boolean values result in an error.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class XorTemplateFunction implements TemplateFunction<Boolean> {

    private boolean xor(Object left, Object right) {
        if (!(left instanceof Boolean)) {
            throw fail("all arguments must be a boolean");
        }
        if (!(right instanceof Boolean)) {
            throw fail("all arguments must be a boolean");
        }
        var x = (boolean) left;
        var y = (boolean) right;
        return x ^ y;
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        return xor(args.get(0), pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        return xor(args.get(0), args.get(1));
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "xor";
    }
}

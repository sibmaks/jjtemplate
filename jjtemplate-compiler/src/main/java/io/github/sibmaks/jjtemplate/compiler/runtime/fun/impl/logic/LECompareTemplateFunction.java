package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import java.util.List;

/**
 * Template function that checks whether one numeric value is less than
 * or equal to another.
 *
 * <p>Uses the comparison logic provided by {@link CompareTemplateFunction}
 * and supports both direct and pipe invocation forms.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class LECompareTemplateFunction extends CompareTemplateFunction {

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        var y = args.get(0);
        return fnCmp(pipeArg, y, -1, true);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var x = args.get(0);
        var y = args.get(1);
        return fnCmp(x, y, -1, true);
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "le";
    }
}

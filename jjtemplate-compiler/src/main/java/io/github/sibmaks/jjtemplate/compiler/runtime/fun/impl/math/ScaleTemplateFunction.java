package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import java.math.RoundingMode;
import java.util.List;

/**
 * Template function that scales a numeric value by a given factor.
 * <p>
 * Evaluates the input number and the scale factor and returns the result
 * of applying the scaling operation.
 * </p>
 *
 * <p>
 * This function is typically used for proportional transformations
 * of numeric values inside templates.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class ScaleTemplateFunction extends MathTemplateFunction {

    private Number scale(Object value, int scale, RoundingMode roundingMode) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number)) {
            throw fail("not a number passed: " + value);
        }
        var number = (Number) value;
        var bigDecimal = toBigDecimal(number);
        return bigDecimal.setScale(scale, roundingMode);
    }

    @Override
    public Number invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var scale = Integer.parseInt(args.get(0).toString());
        var roundMode = toRoundingMode(args.get(1));
        return scale(pipeArg, scale, roundMode);
    }

    @Override
    public Number invoke(List<Object> args) {
        if (args.size() != 3) {
            throw fail("3 arguments required");
        }
        var number = args.get(0);
        var scale = Integer.parseInt(args.get(1).toString());
        var roundMode = toRoundingMode(args.get(2));
        return scale(number, scale, roundMode);
    }

    @Override
    public String getName() {
        return "scale";
    }
}

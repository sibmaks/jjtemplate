package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import java.math.RoundingMode;
import java.util.List;

/**
 * @since 0.4.0
 */
public class ScaleTemplateFunction extends MathTemplateFunction {

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

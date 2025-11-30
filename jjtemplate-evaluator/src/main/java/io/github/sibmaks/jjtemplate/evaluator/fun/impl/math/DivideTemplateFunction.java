package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @since 0.4.0
 */
public final class DivideTemplateFunction extends MathTemplateFunction {

    private Number divide(Object left, Object right, Object rawRoundingMode) {
        if (left == null || right == null) {
            return null;
        }
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw fail("not a number passed");
        }

        var l = (Number) left;
        var r = (Number) right;

        if (l instanceof BigDecimal || r instanceof BigDecimal ||
                l instanceof Double || r instanceof Double ||
                l instanceof Float || r instanceof Float) {
            var lbd = toBigDecimal(l);
            var rbd = toBigDecimal(r);

            var roundingMode = toRoundingMode(rawRoundingMode);
            lbd = lbd.setScale(Math.max(lbd.scale(), rbd.scale()), roundingMode);
            rbd = rbd.setScale(Math.max(lbd.scale(), rbd.scale()), roundingMode);

            return lbd.divide(rbd, roundingMode);
        }

        return toBigInteger(l).divide(toBigInteger(r));
    }

    @Override
    public Number invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        if(args.size() > 2) {
            throw fail("at most 2 arguments required");
        }
        var left = args.get(0);
        var mode = args.size() == 2 ? args.get(1) : null;
        return divide(left, pipeArg, mode);
    }

    @Override
    public Number invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 2 arguments required");
        }
        if(args.size() > 3) {
            throw fail("at most 3 arguments required");
        }
        var left = args.get(0);
        var right = args.get(1);
        var mode = args.size() == 3 ? args.get(2) : null;
        return divide(left, right, mode);
    }

    @Override
    public String getName() {
        return "div";
    }
}

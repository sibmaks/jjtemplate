package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @since 0.4.0
 */
public class SumTemplateFunction implements TemplateFunction<Number> {

    private static BigDecimal toBigDecimal(Number value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        return BigDecimal.valueOf(value.doubleValue());
    }

    private static BigInteger toBigInteger(Number value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        return BigInteger.valueOf(value.longValue());
    }

    private Number sum(Object left, Object right) {
        if (left == null || right == null) {
            return null;
        }
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw fail("not a number passed");
        }

        var l = (Number) left;
        var r = (Number) right;

        if (l instanceof BigDecimal || r instanceof BigDecimal) {
            return toBigDecimal(l).add(toBigDecimal(r));
        }

        if (l instanceof BigInteger || r instanceof BigInteger) {
            return toBigInteger(l).add(toBigInteger(r));
        }

        if (l instanceof Double || r instanceof Double || l instanceof Float || r instanceof Float) {
            return toBigDecimal(l).add(toBigDecimal(r));
        }

        return toBigInteger(l).add(toBigInteger(r));
    }

    @Override
    public Number invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var left = args.get(0);
        return sum(left, pipeArg);
    }

    @Override
    public Number invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("2 arguments required");
        }
        if (args.size() != 2) {
            throw fail("too much arguments passed");
        }
        var left = args.get(0);
        var right = args.get(1);
        return sum(left, right);
    }

    @Override
    public String getNamespace() {
        return "math";
    }

    @Override
    public String getName() {
        return "sum";
    }
}

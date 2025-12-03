package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Base interface for binary mathematical template functions.
 * <p>
 * Represents an operation that takes two numeric arguments and produces
 * a numeric result. Implementations define the concrete mathematical
 * operation (for example: addition, subtraction, multiplication).
 * </p>
 *
 * <p>
 * Implementations are expected to handle numeric type normalization
 * consistently.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public abstract class MathTemplateBiFunction extends MathTemplateFunction {
    /**
     * Applies the mathematical operation to the given operands.
     *
     * @param left  left operand
     * @param right right operand
     * @return result of the mathematical operation
     */
    protected abstract BigDecimal execute(BigDecimal left, BigDecimal right);

    /**
     * Applies the mathematical operation to the given operands.
     *
     * @param left  left operand
     * @param right right operand
     * @return result of the mathematical operation
     */
    protected abstract BigInteger execute(BigInteger left, BigInteger right);

    /**
     * Applies the mathematical operation to the given operands.
     *
     * @param left  left operand
     * @param right right operand
     * @return result of the mathematical operation
     */
    protected Number call(Object left, Object right) {
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
            var leftBigDecimal = toBigDecimal(l);
            var rightBigDecimal = toBigDecimal(r);
            return execute(leftBigDecimal, rightBigDecimal);
        }

        var leftBigInteger = toBigInteger(l);
        var rightBigInteger = toBigInteger(r);
        return execute(leftBigInteger, rightBigInteger);
    }

    @Override
    public final Number invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var left = args.get(0);
        return call(left, pipeArg);
    }

    @Override
    public final Number invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("2 arguments required");
        }
        if (args.size() != 2) {
            throw fail("too much arguments passed");
        }
        var left = args.get(0);
        var right = args.get(1);
        return call(left, right);
    }
}

package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Template function that returns the numeric negation of a value.
 *
 * <p>Supports {@link BigDecimal}, {@link BigInteger}, and all {@link Number} types.
 * Returns {@code null} for {@code null} input.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class NegTemplateFunction extends MathTemplateFunction {

    private Number neg(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number)) {
            throw fail("not a number passed: " + value);
        }
        var number = (Number) value;
        if (number instanceof BigDecimal || number instanceof Double || number instanceof Float) {
            var bigDecimal = toBigDecimal(number);
            return bigDecimal.negate();
        }
        var bigInteger = toBigInteger(number);
        return bigInteger.negate();
    }

    @Override
    public Number invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return neg(pipeArg);
    }

    @Override
    public Number invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return neg(args.get(0));
    }

    @Override
    public String getName() {
        return "neg";
    }
}

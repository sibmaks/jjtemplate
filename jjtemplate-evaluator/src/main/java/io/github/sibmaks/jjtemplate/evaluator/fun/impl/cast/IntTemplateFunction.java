package io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public final class IntTemplateFunction implements TemplateFunction<BigInteger> {
    private static BigInteger toInt(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof BigInteger) {
            return (BigInteger) arg;
        }
        if (arg instanceof BigDecimal) {
            var bigDecimal = (BigDecimal) arg;
            return bigDecimal.toBigInteger();
        }
        if (arg instanceof Number) {
            var doubleValue = ((Number) arg).longValue();
            return BigInteger.valueOf(doubleValue);
        }
        if (arg instanceof String) {
            try {
                return new BigInteger((String) arg);
            } catch (Exception e) {
                throw new TemplateEvalException("int: cannot convert: " + arg, e);
            }
        }
        throw new TemplateEvalException("int: cannot convert: " + arg);
    }

    @Override
    public BigInteger invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("int: too much arguments passed");
        }
        return toInt(pipeArg);
    }

    @Override
    public BigInteger invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("int: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("int: too much arguments passed");
        }
        var arg = args.get(0);
        return toInt(arg);
    }

    @Override
    public String getName() {
        return "int";
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public final class FloatTemplateFunction implements TemplateFunction<BigDecimal> {
    private static BigDecimal toFloat(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof BigDecimal) {
            return (BigDecimal) arg;
        }
        if (arg instanceof Long) {
            var longValue = (Long) arg;
            return BigDecimal.valueOf(longValue);
        }
        if (arg instanceof Number) {
            var doubleValue = ((Number) arg).doubleValue();
            return BigDecimal.valueOf(doubleValue);
        }
        if (arg instanceof String) {
            try {
                return new BigDecimal((String) arg);
            } catch (Exception e) {
                throw new TemplateEvalException("float: cannot convert: " + arg, e);
            }
        }
        throw new TemplateEvalException("float: cannot convert: " + arg);
    }

    @Override
    public BigDecimal invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("float: too much arguments passed");
        }
        return toFloat(pipeArg);
    }

    @Override
    public BigDecimal invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("float: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("float: too much arguments passed");
        }
        var arg = args.get(0);
        return toFloat(arg);
    }

    @Override
    public String getName() {
        return "float";
    }
}

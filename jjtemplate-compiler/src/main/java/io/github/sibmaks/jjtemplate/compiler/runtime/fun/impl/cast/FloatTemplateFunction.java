package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.cast;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Template function that converts a value to {@link BigDecimal}.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class FloatTemplateFunction implements TemplateFunction<BigDecimal> {
    private BigDecimal toFloat(Object arg) {
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
                throw fail("cannot convert: " + arg, e);
            }
        }
        throw fail("cannot convert: " + arg);
    }

    @Override
    public BigDecimal invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return toFloat(pipeArg);
    }

    @Override
    public BigDecimal invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var arg = args.get(0);
        return toFloat(arg);
    }

    @Override
    public String getNamespace() {
        return "cast";
    }

    @Override
    public String getName() {
        return "float";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

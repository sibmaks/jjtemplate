package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Template function that checks two values for equality using {@link Objects#equals}.
 *
 * <p>Supports both direct and pipe invocation forms.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class EqualsTemplateFunction implements TemplateFunction<Boolean> {
    private boolean areEquals(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return asNum((Number) left).compareTo(asNum((Number) right)) == 0;
        }
        return Objects.equals(left, right);
    }

    private BigDecimal asNum(Number value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return BigDecimal.valueOf(value.longValue());
        }
        return BigDecimal.valueOf(value.doubleValue());
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        return areEquals(args.get(0), pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        return areEquals(args.get(0), args.get(1));
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getName() {
        return "eq";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.logic;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Base class for numeric comparison template functions.
 *
 * <p>Provides common logic for converting values to {@link BigDecimal}
 * and performing directional comparisons with optional equality.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public abstract class CompareTemplateFunction implements TemplateFunction<Boolean> {

    protected boolean fnCmp(Object x, Object y, int dir, boolean eq) {
        var nx = asNum(x);
        var ny = asNum(y);
        var c = nx.compareTo(ny);
        if (dir < 0) {
            return eq ? c <= 0 : c < 0;
        }
        return eq ? c >= 0 : c > 0;
    }

    protected BigDecimal asNum(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            var bigInteger = (BigInteger) value;
            return new BigDecimal(bigInteger);
        }
        if (value instanceof Long) {
            var longValue = ((Number) value).longValue();
            return BigDecimal.valueOf(longValue);
        }
        if (value instanceof Number) {
            var doubleValue = ((Number) value).doubleValue();
            return BigDecimal.valueOf(doubleValue);
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (Exception e) {
                throw fail("expected number, actual: " + value, e);
            }
        }
        throw fail("expected number, actual: " + value);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

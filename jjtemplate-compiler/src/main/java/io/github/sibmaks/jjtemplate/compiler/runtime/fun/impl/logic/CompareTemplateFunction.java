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

    /**
     * Creates a template function instance.
     */
    protected CompareTemplateFunction() {
        // No initialization is required because this implementation is stateless.
    }

    /**
     * Compares two numeric values in the requested direction.
     *
     * @param x left value
     * @param y right value
     * @param dir negative for less-than comparison, non-negative for greater-than comparison
     * @param eq whether equality satisfies the comparison
     * @return comparison result
     */
    protected boolean fnCmp(Object x, Object y, int dir, boolean eq) {
        var nx = asNum(x);
        var ny = asNum(y);
        var c = nx.compareTo(ny);
        if (dir < 0) {
            return eq ? c <= 0 : c < 0;
        }
        return eq ? c >= 0 : c > 0;
    }

    /**
     * Converts a supported value to a decimal number.
     *
     * @param value value to convert
     * @return decimal representation
     */
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

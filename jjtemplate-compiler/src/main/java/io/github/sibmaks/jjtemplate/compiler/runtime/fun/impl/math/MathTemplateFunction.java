package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 *
 * @author sibmaks
 * @since 0.4.0
 */
public abstract class MathTemplateFunction implements TemplateFunction<Number> {

    /**
     * Convert any number into BigDecimal
     *
     * @param value raw number
     * @return converted value
     */
    protected static BigDecimal toBigDecimal(Number value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }
        return BigDecimal.valueOf(value.doubleValue());
    }

    /**
     * Convert any number into BigInteger
     *
     * @param value raw number
     * @return converted value
     */
    protected static BigInteger toBigInteger(Number value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        return BigInteger.valueOf(value.longValue());
    }

    /**
     * Convert an object into RoundingMode
     *
     * @param value raw value
     * @return converted value
     */
    protected RoundingMode toRoundingMode(Object value) {
        if (value == null) {
            throw fail("null cannot be converted to rounding mode");
        }
        if (value instanceof RoundingMode) {
            return (RoundingMode) value;
        }
        var string = value.toString();
        return RoundingMode.valueOf(string);
    }

    @Override
    public String getNamespace() {
        return "math";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
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

    @Override
    public String getNamespace() {
        return "math";
    }
}

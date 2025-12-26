package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Template function that performs numeric subtraction.
 * <p>
 * Evaluates two numeric arguments and returns the result of subtracting
 * the second value from the first one.
 * </p>
 *
 * <p>
 * The operation follows standard Java subtraction semantics for the
 * underlying numeric types.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class SubTemplateFunction extends MathTemplateBiFunction {

    @Override
    public String getName() {
        return "sub";
    }

    @Override
    protected BigDecimal execute(BigDecimal left, BigDecimal right) {
        return left.subtract(right);
    }

    @Override
    protected BigInteger execute(BigInteger left, BigInteger right) {
        return left.subtract(right);
    }
}

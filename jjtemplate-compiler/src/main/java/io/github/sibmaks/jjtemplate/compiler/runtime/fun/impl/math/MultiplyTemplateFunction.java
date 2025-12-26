package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Template function that performs numeric multiplication.
 * <p>
 * Evaluates two numeric arguments and returns the result of multiplying
 * them together.
 * </p>
 *
 * <p>
 * The operation follows standard Java multiplication semantics for the
 * underlying numeric types.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class MultiplyTemplateFunction extends MathTemplateBiFunction {

    @Override
    public String getName() {
        return "mul";
    }

    @Override
    protected BigDecimal execute(BigDecimal left, BigDecimal right) {
        return left.multiply(right);
    }

    @Override
    protected BigInteger execute(BigInteger left, BigInteger right) {
        return left.multiply(right);
    }
}

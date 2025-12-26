package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Template function that performs numeric addition.
 * <p>
 * Evaluates two numeric arguments and returns their sum.
 * </p>
 *
 * <p>
 * The operation follows standard Java addition semantics for the
 * underlying numeric types.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class SumTemplateFunction extends MathTemplateBiFunction {

    @Override
    public String getName() {
        return "sum";
    }

    @Override
    protected BigDecimal execute(BigDecimal left, BigDecimal right) {
        return left.add(right);
    }

    @Override
    protected BigInteger execute(BigInteger left, BigInteger right) {
        return left.add(right);
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
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

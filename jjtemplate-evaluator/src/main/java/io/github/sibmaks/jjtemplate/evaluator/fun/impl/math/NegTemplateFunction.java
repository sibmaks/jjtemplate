package io.github.sibmaks.jjtemplate.evaluator.fun.impl.math;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class NegTemplateFunction implements TemplateFunction<Number> {

    private static Number neg(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            var bigDecimal = (BigDecimal) value;
            return bigDecimal.negate();
        }
        if (value instanceof BigInteger) {
            var bigInteger = (BigInteger) value;
            return bigInteger.negate();
        }
        if (value instanceof Double || value instanceof Float) {
            var number = (Number) value;
            var doubleValue = number.doubleValue();
            return BigDecimal.valueOf(doubleValue)
                    .negate();
        }
        if (value instanceof Number) {
            var number = (Number) value;
            var longValue = number.longValue();
            return BigInteger.valueOf(longValue)
                    .negate();
        }
        throw new TemplateEvalException("neg: not a number passed: " + value);
    }

    @Override
    public Number invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("neg: too much arguments passed");
        }
        return neg(pipeArg);
    }

    @Override
    public Number invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("neg: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("neg: too much arguments passed");
        }
        return neg(args.get(0));
    }

    @Override
    public String getName() {
        return "neg";
    }
}

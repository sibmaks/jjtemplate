package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author sibmaks
 */
public abstract class CompareTemplateFunction implements TemplateFunction {

    protected boolean fnCmp(List<ExpressionValue> args, ExpressionValue pipe, int dir, boolean eq) {
        ExpressionValue x;
        ExpressionValue y;
        if (args.size() == 1 && !pipe.isEmpty()) {
            x = pipe;
            y = args.get(0);
        } else if (args.size() == 2 && pipe.isEmpty()) {
            x = args.get(0);
            y = args.get(1);
        } else {
            throw new TemplateEvalException("cmp: invalid args");
        }
        var nx = asNum(x.getValue());
        var ny = asNum(y.getValue());
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
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (Exception e) {
                throw new TemplateEvalException("Expected number: " + value, e);
            }
        }
        throw new TemplateEvalException("Expected number: " + value);
    }
}

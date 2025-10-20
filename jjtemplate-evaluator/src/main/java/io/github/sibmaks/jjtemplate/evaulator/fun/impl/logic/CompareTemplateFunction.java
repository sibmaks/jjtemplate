package io.github.sibmaks.jjtemplate.evaulator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaulator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public abstract class CompareTemplateFunction implements TemplateFunction {

    protected boolean fnCmp(List<ExpressionValue> args, ExpressionValue pipe, int dir, boolean eq) {
        ExpressionValue x, y;
        if (args.size() == 1) {
            x = pipe;
            y = args.get(0);
        } else if (args.size() == 2) {
            x = args.get(0);
            y = args.get(1);
        } else {
            throw new TemplateEvalException("cmp: invalid args");
        }
        var nx = asNum(x.getValue());
        var ny = asNum(y.getValue());
        var c = Double.compare(nx.doubleValue(), ny.doubleValue());
        return dir < 0 ? (eq ? c <= 0 : c < 0) : (eq ? c >= 0 : c > 0);
    }

    protected Number asNum(Object v) {
        if (v instanceof Number) {
            return (Number) v;
        }
        if (v instanceof String) {
            try {
                return Double.parseDouble((String) v);
            } catch (Exception ignored) {
            }
        }
        throw new TemplateEvalException("Expected number: " + v);
    }
}

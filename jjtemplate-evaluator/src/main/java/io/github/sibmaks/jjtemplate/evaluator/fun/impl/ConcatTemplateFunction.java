package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Concatenation based on 1st argument type
 *
 * @author sibmaks
 */
public class ConcatTemplateFunction implements TemplateFunction {
    private static ExpressionValue concatString(ArrayList<ExpressionValue> all) {
        var sb = new StringBuilder();
        for (var v : all) {
            sb.append(v.getValue());
        }
        return ExpressionValue.of(sb.toString());
    }

    private static ExpressionValue concatCollection(
            Collection<?> collection,
            ArrayList<ExpressionValue> all
    ) {
        var out = new ArrayList<Object>(collection);
        for (var i = 1; i < all.size(); i++) {
            var v = all.get(i);
            if (v instanceof Collection<?>) {
                var subCollection = (Collection<?>) v;
                out.addAll(subCollection);
            } else {
                out.add(v.getValue());
            }
        }
        return ExpressionValue.of(out);
    }

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var all = new ArrayList<>(args);
        if (!pipeArg.isEmpty()) {
            all.add(pipeArg);
        }
        var first = all.isEmpty() ? ExpressionValue.empty() : all.get(0);
        if (first.getValue() instanceof Collection) {
            return concatCollection((Collection<?>) first.getValue(), all);
        } else {
            return concatString(all);
        }
    }

    @Override
    public String getName() {
        return "concat";
    }
}

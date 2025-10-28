package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
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
            List<ExpressionValue> all
    ) {
        var out = new ArrayList<Object>(collection);
        collectSubArgs(all, out);
        return ExpressionValue.of(out);
    }


    private static ExpressionValue concatArray(
            Object array,
            List<ExpressionValue> all
    ) {
        var len = Array.getLength(array);
        var out = new ArrayList<>(len + all.size());
        for (var i = 0; i < len; i++) {
            var item = Array.get(array, i);
            out.add(item);
        }
        collectSubArgs(all, out);
        return ExpressionValue.of(out);
    }

    private static void collectSubArgs(
            List<ExpressionValue> all,
            List<Object> out
    ) {
        for (var i = 1; i < all.size(); i++) {
            var expressionValue = all.get(i);
            var item = expressionValue.getValue();
            if (item instanceof Collection<?>) {
                var subCollection = (Collection<?>) item;
                out.addAll(subCollection);
            } else if (item != null && item.getClass().isArray()) {
                var subArrayLen = Array.getLength(item);
                for (var j = 0; j < subArrayLen; j++) {
                    out.add(Array.get(item, j));
                }
            } else {
                out.add(item);
            }
        }
    }

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var all = new ArrayList<>(args);
        if (!pipeArg.isEmpty()) {
            all.add(pipeArg);
        }
        var first = all.isEmpty() ? ExpressionValue.empty() : all.get(0);
        var firstValue = first.getValue();
        if (firstValue instanceof Collection) {
            return concatCollection((Collection<?>) firstValue, all);
        } else if (firstValue != null && firstValue.getClass().isArray()) {
            return concatArray(firstValue, all);
        }
        return concatString(all);
    }

    @Override
    public String getName() {
        return "concat";
    }
}

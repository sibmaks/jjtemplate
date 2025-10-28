package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concatenation based on 1st argument type
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class ConcatTemplateFunction implements TemplateFunction {
    private static ExpressionValue concatString(List<Object> all) {
        var sb = new StringBuilder();
        for (var v : all) {
            sb.append(v);
        }
        return ExpressionValue.of(sb.toString());
    }

    private static ExpressionValue concatCollection(
            Collection<?> collection,
            List<Object> all
    ) {
        var out = new ArrayList<Object>(collection);
        collectSubArgs(all, out);
        return ExpressionValue.of(out);
    }


    private static ExpressionValue concatArray(
            Object array,
            List<Object> all
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
            List<Object> all,
            List<Object> out
    ) {
        for (var i = 1; i < all.size(); i++) {
            var item = all.get(i);
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
        var all = args.stream()
                .filter(it -> !it.isEmpty())
                .map(ExpressionValue::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
        if (!pipeArg.isEmpty()) {
            all.add(pipeArg.getValue());
        }
        if(all.isEmpty()) {
            throw new TemplateEvalException("concat: at least 1 argument required");
        }
        var first = all.get(0);
        if (first instanceof Collection) {
            return concatCollection((Collection<?>) first, all);
        } else if (first != null && first.getClass().isArray()) {
            return concatArray(first, all);
        }
        return concatString(all);
    }

    @Override
    public String getName() {
        return "concat";
    }
}

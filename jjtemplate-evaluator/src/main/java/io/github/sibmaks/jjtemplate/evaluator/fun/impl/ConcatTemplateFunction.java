package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
public class ConcatTemplateFunction implements TemplateFunction<Object> {
    private static String concatString(Object first, List<Object> all) {
        var sb = new StringBuilder(String.valueOf(first));
        for (var v : all) {
            sb.append(v);
        }
        return sb.toString();
    }

    private static List<Object> concatCollection(
            Collection<?> collection,
            List<Object> all
    ) {
        var out = new ArrayList<Object>(collection);
        collectSubArgs(all, out);
        return out;
    }

    private static List<Object> concatArray(
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
        return out;
    }

    private static void collectSubArgs(
            List<Object> all,
            List<Object> out
    ) {
        for (var item : all) {
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

    private static Object concat(Object first, List<Object> args) {
        if (first instanceof Collection) {
            return concatCollection((Collection<?>) first, args);
        } else if (first != null && first.getClass().isArray()) {
            return concatArray(first, args);
        }
        return concatString(first, args);
    }

    @Override
    public Object invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("concat: at least 1 argument required");
        }
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        all.add(pipeArg);
        return concat(args.get(0), all);
    }

    @Override
    public Object invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("concat: at least 1 argument required");
        }
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        return concat(args.get(0), all);
    }

    @Override
    public String getName() {
        return "concat";
    }
}

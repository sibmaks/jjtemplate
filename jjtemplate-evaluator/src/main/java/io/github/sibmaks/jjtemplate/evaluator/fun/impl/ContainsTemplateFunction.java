package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.1.2
 */
public class ContainsTemplateFunction implements TemplateFunction {
    private static ExpressionValue containsString(String line, List<Object> all) {
        for (var item : all) {
            if (!line.contains(item.toString())) {
                return ExpressionValue.of(false);
            }
        }
        return ExpressionValue.of(true);
    }

    private static ExpressionValue containsCollection(
            Collection<?> collection,
            List<Object> all
    ) {
        for (var item : collection) {
            if (!all.contains(item)) {
                return ExpressionValue.of(false);
            }
        }
        return ExpressionValue.of(true);
    }

    private static ExpressionValue containsMap(
            Map<?, ?> collection,
            List<Object> all
    ) {
        for (var item : collection.keySet()) {
            if (!all.contains(item)) {
                return ExpressionValue.of(false);
            }
        }
        return ExpressionValue.of(true);
    }


    private static ExpressionValue containsArray(
            Object array,
            List<Object> all
    ) {
        var len = Array.getLength(array);
        for (var i = 0; i < len; i++) {
            var item = Array.get(array, i);
            if (!all.contains(item)) {
                return ExpressionValue.of(false);
            }
        }
        return ExpressionValue.of(true);
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
        if (all.size() < 2) {
            throw new TemplateEvalException("contains: at least 2 arguments required");
        }
        var first = all.get(0);
        if (first == null) {
            return ExpressionValue.of(false);
        }
        var subArgs = all.subList(1, all.size());
        if (first instanceof Collection) {
            return containsCollection((Collection<?>) first, subArgs);
        }
        if (first instanceof Map<?, ?>) {
            return containsMap((Map<?, ?>) first, subArgs);
        }
        if (first.getClass().isArray()) {
            return containsArray(first, subArgs);
        }
        if(first instanceof String) {
            var line = String.valueOf(first);
            return containsString(line, subArgs);
        }
        throw new TemplateEvalException("contains: first argument of unsupported type " + first.getClass());
    }

    @Override
    public String getName() {
        return "contains";
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author sibmaks
 * @since 0.1.2
 */
public class ContainsTemplateFunction implements TemplateFunction<Boolean> {
    private static boolean containsString(String line, Set<Object> all) {
        for (var item : all) {
            if (!line.contains(item.toString())) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsCollection(
            Collection<?> collection,
            Set<Object> all
    ) {
        for (var item : collection) {
            all.remove(item);
        }
        return all.isEmpty();
    }

    private static boolean containsMap(
            Map<?, ?> collection,
            Set<Object> all
    ) {
        for (var item : collection.keySet()) {
            all.remove(item);
        }
        return all.isEmpty();
    }


    private static boolean containsArray(
            Object array,
            Set<Object> all
    ) {
        var len = Array.getLength(array);
        for (var i = 0; i < len; i++) {
            var item = Array.get(array, i);
            all.remove(item);
        }
        return all.isEmpty();
    }

    private static boolean contains(Object value, Set<Object> args) {
        if (value instanceof Collection) {
            return containsCollection((Collection<?>) value, args);
        }
        if (value instanceof Map<?, ?>) {
            return containsMap((Map<?, ?>) value, args);
        }
        if (value.getClass().isArray()) {
            return containsArray(value, args);
        }
        if (value instanceof String) {
            var line = String.valueOf(value);
            return containsString(line, args);
        }
        throw new TemplateEvalException("contains: first argument of unsupported type " + value.getClass());
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("contains: at least 1 argument required");
        }
        return contains(pipeArg, new HashSet<>(args));
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() < 2) {
            throw new TemplateEvalException("contains: at least 2 arguments required");
        }
        var container = args.get(0);
        var toCheck = args.subList(1, args.size());
        return contains(container, new HashSet<>(toCheck));
    }

    @Override
    public String getName() {
        return "contains";
    }
}

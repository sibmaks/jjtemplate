package io.github.sibmaks.jjtemplate.evaluator.fun.impl.map;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Template function that checks whether a map contains all specified keys.
 *
 * <p>Supports direct and pipe forms. Only map keys are considered; values
 * are ignored.</p>
 *
 * @author sibmaks
 * @since 0.1.2
 */
public final class MapContainsTemplateFunction implements TemplateFunction<Boolean> {

    private static boolean containsMap(
            Map<?, ?> collection,
            Set<Object> all
    ) {
        for (var item : collection.keySet()) {
            all.remove(item);
        }
        return all.isEmpty();
    }

    private boolean contains(Object value, Set<Object> args) {
        if (value instanceof Map<?, ?>) {
            return containsMap((Map<?, ?>) value, args);
        }
        throw fail("1st argument of unsupported type " + value.getClass());
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        return contains(pipeArg, new HashSet<>(args));
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        var container = args.get(0);
        var toCheck = args.subList(1, args.size());
        return contains(container, new HashSet<>(toCheck));
    }

    @Override
    public String getNamespace() {
        return "map";
    }

    @Override
    public String getName() {
        return "contains";
    }
}

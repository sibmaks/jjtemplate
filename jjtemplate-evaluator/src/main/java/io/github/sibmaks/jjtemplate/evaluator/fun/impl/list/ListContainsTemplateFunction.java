package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Template function that checks whether a collection or array contains all specified values.
 *
 * <p>Supported container types:
 * <ul>
 *   <li>{@link Collection}</li>
 *   <li>arrays of any component type</li>
 * </ul>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public class ListContainsTemplateFunction implements TemplateFunction<Boolean> {

    private static boolean containsCollection(
            Collection<?> collection,
            Set<Object> all
    ) {
        for (var item : collection) {
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

    private boolean contains(Object value, Set<Object> args) {
        if (value instanceof Collection) {
            return containsCollection((Collection<?>) value, args);
        }
        if (value.getClass().isArray()) {
            return containsArray(value, args);
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
        return "list";
    }

    @Override
    public String getName() {
        return "contains";
    }
}

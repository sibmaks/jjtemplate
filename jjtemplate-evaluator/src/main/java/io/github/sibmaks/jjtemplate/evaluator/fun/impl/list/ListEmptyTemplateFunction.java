package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * Template function that checks whether a collection or array is empty.
 *
 * <p>Supports {@link Collection} instances and arrays. Scalars are not allowed.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class ListEmptyTemplateFunction implements TemplateFunction<Boolean> {

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        throw fail("unsupported type: " + value.getClass());
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return isEmpty(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return isEmpty(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "empty";
    }
}

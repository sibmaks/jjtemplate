package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * Template function that returns the size of a collection or array.
 *
 * <p>Returns {@code 0} for {@code null} values. Scalars are not supported.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public class ListLengthTemplateFunction implements TemplateFunction<Integer> {

    private int getLength(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        throw fail("unsupported type: " + value.getClass());
    }

    @Override
    public Integer invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return getLength(pipeArg);
    }

    @Override
    public Integer invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return getLength(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "len";
    }
}

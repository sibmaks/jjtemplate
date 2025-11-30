package io.github.sibmaks.jjtemplate.evaluator.fun.impl.map;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Map;

/**
 * Template function that returns the number of entries in a map.
 *
 * <p>Returns {@code 0} for {@code null}. Only {@link Map} values are supported;
 * other types result in an error.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class MapLengthTemplateFunction implements TemplateFunction<Integer> {

    private int getLength(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).size();
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
        return "map";
    }

    @Override
    public String getName() {
        return "len";
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.map;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Map;

/**
 * Template function that checks whether a map is empty.
 *
 * <p>Returns {@code true} for {@code null} and empty {@link Map} values.
 * Other types are not supported.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class MapEmptyTemplateFunction implements TemplateFunction<Boolean> {

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
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
        return "map";
    }

    @Override
    public String getName() {
        return "empty";
    }
}

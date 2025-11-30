package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public final class ListTailTemplateFunction implements TemplateFunction<List<Object>> {

    private List<Object> getTail(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection) {
            var collection = ((Collection<?>) value);
            return collection.stream()
                    .skip(1)
                    .collect(Collectors.toList());
        }
        if (value.getClass().isArray()) {
            var len = Array.getLength(value);
            var rs = new ArrayList<>(len);
            for (int i = 1; i < len; i++) {
                var item = Array.get(value, i);
                rs.add(item);
            }
            return rs;
        }
        throw fail("unsupported type: " + value.getClass());
    }

    @Override
    public List<Object> invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return getTail(pipeArg);
    }

    @Override
    public List<Object> invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return getTail(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "tail";
    }
}

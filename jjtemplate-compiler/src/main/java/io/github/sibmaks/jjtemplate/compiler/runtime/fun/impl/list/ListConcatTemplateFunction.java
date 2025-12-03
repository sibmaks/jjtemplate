package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Template function that concatenates multiple list-like values into a single list.
 *
 * <p>Supports collections, arrays, and scalar values. Scalars are appended as-is.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class ListConcatTemplateFunction implements TemplateFunction<List<Object>> {

    private List<Object> concat(List<Object> args) {
        var contacted = new ArrayList<>();
        for (var arg : args) {
            if (arg == null) {
                contacted.add(null);
            } else if (arg instanceof Collection) {
                var collection = (Collection<?>) arg;
                contacted.addAll(collection);
            } else if (arg.getClass().isArray()) {
                var len = Array.getLength(arg);
                for (var i = 0; i < len; i++) {
                    var item = Array.get(arg, i);
                    contacted.add(item);
                }
            } else {
                contacted.add(arg);
            }
        }
        return contacted;
    }

    @Override
    public List<Object> invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var all = new ArrayList<>(args);
        all.add(pipeArg);
        return concat(all);
    }

    @Override
    public List<Object> invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        return concat(args);
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "concat";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl.map;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Template function that constructs a new map from key–value pairs.
 *
 * <p>Arguments are interpreted in pairs. In pipe form, an odd trailing key
 * receives the piped value as its value.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class MapNewTemplateFunction implements TemplateFunction<Map<?, ?>> {

    @Override
    public Map<?, ?> invoke(List<Object> args, Object pipeArg) {
        var size = args.size();
        var out = new LinkedHashMap<>(size + 1);
        for (int i = 0; i < size - 1; i += 2) {
            var key = args.get(i);
            var value = args.get(i + 1);
            out.put(key, value);
        }
        if (size % 2 == 1) {
            var key = args.get(size - 1);
            out.put(key, pipeArg);
        }
        return out;
    }

    @Override
    public Map<?, ?> invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var out = new LinkedHashMap<>(args.size() + 1);
        for (int i = 0; i < args.size() - 1; i += 2) {
            var key = args.get(i);
            var value = args.get(i + 1);
            out.put(key, value);
        }
        return out;
    }

    @Override
    public String getNamespace() {
        return "map";
    }

    @Override
    public String getName() {
        return "new";
    }
}

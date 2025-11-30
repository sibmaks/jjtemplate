package io.github.sibmaks.jjtemplate.evaluator.fun.impl.map;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Template function that extracts and merges object properties into a single map.
 *
 * <p>Supports collapsing properties from objects, collections, and arrays.
 * Properties inherited from {@link Object} are ignored.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class CollapseTemplateFunction implements TemplateFunction<Map<String, Object>> {
    private static final Map<String, Object> OBJECT_PROPERTIES = ReflectionUtils.getAllProperties(new Object());

    private static Map<String, Object> getProperties(Object value) {
        if (value == null) {
            return Collections.emptyMap();
        }
        var result = new LinkedHashMap<String, Object>();
        if (value instanceof Collection) {
            var collection = (Collection<?>) value;
            for (var o : collection) {
                result.putAll(getProperties(o));
            }
            return result;
        } else if (value.getClass().isArray()) {
            var len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                var item = Array.get(value, i);
                result.putAll(getProperties(item));
            }
            return result;
        }

        var fields = new HashMap<>(ReflectionUtils.getAllProperties(value));
        for (var key : OBJECT_PROPERTIES.keySet()) {
            fields.remove(key);
        }
        return fields;
    }

    @Override
    public Map<String, Object> invoke(List<Object> args, Object pipeArg) {
        var result = new LinkedHashMap<String, Object>(args.size());

        result.putAll(getProperties(pipeArg));

        for (var arg : args) {
            result.putAll(getProperties(arg));
        }

        return result;
    }

    @Override
    public Map<String, Object> invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var result = new LinkedHashMap<String, Object>(args.size());

        for (var arg : args) {
            result.putAll(getProperties(arg));
        }

        return result;
    }

    @Override
    public String getNamespace() {
        return "map";
    }

    @Override
    public String getName() {
        return "collapse";
    }
}

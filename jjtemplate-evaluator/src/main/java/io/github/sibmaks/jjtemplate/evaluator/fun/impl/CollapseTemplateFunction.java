package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class CollapseTemplateFunction implements TemplateFunction<Map<String, Object>> {
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
            throw new TemplateEvalException("collapse: at least 1 argument required");
        }
        var result = new LinkedHashMap<String, Object>(args.size());

        for (var arg : args) {
            result.putAll(getProperties(arg));
        }

        return result;
    }

    @Override
    public String getName() {
        return "collapse";
    }
}

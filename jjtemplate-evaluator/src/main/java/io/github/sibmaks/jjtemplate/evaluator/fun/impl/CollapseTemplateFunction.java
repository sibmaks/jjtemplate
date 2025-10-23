package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;

import java.util.*;

/**
 *
 * @author sibmaks
 */
public class CollapseTemplateFunction implements TemplateFunction {
    private static final Map<String, Object> OBJECT_PROPERTIES = ReflectionUtils.getAllProperties(new Object());

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.isEmpty() && pipeArg.isEmpty()) {
            throw new IllegalArgumentException("collapse: at least 1 argument required");
        }
        var result = new LinkedHashMap<String, Object>(args.size() + 1);

        for (var arg : args) {
            result.putAll(getProperties(arg.getValue()));
        }

        if(!pipeArg.isEmpty()) {
            result.putAll(getProperties(pipeArg.getValue()));
        }

        return ExpressionValue.of(result);
    }

    private Map<String, Object> getProperties(Object value) {
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
        }

        var fields = ReflectionUtils.getAllProperties(value);
        for (var key : OBJECT_PROPERTIES.keySet()) {
            fields.remove(key);
        }
        return fields;
    }

    @Override
    public String getName() {
        return "collapse";
    }
}

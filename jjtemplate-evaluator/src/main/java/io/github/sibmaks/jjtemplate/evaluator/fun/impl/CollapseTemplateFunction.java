package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 * @author sibmaks
 */
public class CollapseTemplateFunction implements TemplateFunction {
    private final TemplateEvaluator evaluator;

    public CollapseTemplateFunction(TemplateEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.isEmpty() && pipeArg.isEmpty()) {
            throw new IllegalArgumentException("collapse: at least 1 argument required");
        }
        var result = new LinkedHashMap<String, Object>();

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
        } else if (value instanceof Map<?, ?>) {
            var map = (Map<?, ?>) value;
            for (var entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }

        var type = value.getClass();
        var fields = evaluator.getFields(type);
        for (var entry : fields.entrySet()) {
            var fieldName = entry.getKey();
            var field = entry.getValue();
            try {
                var fieldValue = field.get(value);
                result.put(fieldName, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        var methods = evaluator.getMethods(type);
        for (var entry : methods.entrySet()) {
            var methodName = entry.getKey();
            var method = entry.getValue();
            try {
                var methodValue = method.invoke(value);
                result.put(methodName, methodValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    @Override
    public String getName() {
        return "collapse";
    }
}

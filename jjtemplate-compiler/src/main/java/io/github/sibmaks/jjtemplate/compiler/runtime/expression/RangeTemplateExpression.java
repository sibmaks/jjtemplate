package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Template expression that iterates over a collection, array, or map and produces a list of results.
 * <p>
 * The source expression is evaluated first and must yield a {@link java.util.Map},
 * {@link java.util.Collection}, or an array. For collections and arrays the
 * iteration variables represent item and index; for maps they represent key and
 * value. For each element, a new temporary scope is pushed into the
 * {@link Context}, exposing iteration variables, and the body expression is evaluated.
 * </p>
 *
 * <p>
 * The result of each iteration is collected into a new list which is returned
 * as the final value of this expression.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public final class RangeTemplateExpression implements TemplateExpression {
    private final TemplateExpression name;
    private final String firstVariableName;
    private final String secondVariableName;
    private final TemplateExpression source;
    private final TemplateExpression body;
    private final String sourceExpression;

    /**
     * Creates a range expression.
     *
     * @param name               result-name expression
     * @param firstVariableName  item variable for collections and arrays, key variable for maps
     * @param secondVariableName index variable for collections and arrays, value variable for maps
     * @param source             range source
     * @param body               expression evaluated for each item
     * @param sourceExpression   original source expression
     */
    public RangeTemplateExpression(
            TemplateExpression name,
            String firstVariableName,
            String secondVariableName,
            TemplateExpression source,
            TemplateExpression body,
            String sourceExpression
    ) {
        this.name = name;
        this.firstVariableName = firstVariableName;
        this.secondVariableName = secondVariableName;
        this.source = source;
        this.body = body;
        this.sourceExpression = sourceExpression;
    }

    @Override
    public Object apply(final Context context) {
        try {
            var sourceObject = source.apply(context);
            if (sourceObject == null) {
                return null;
            }
            if (sourceObject instanceof Map) {
                return evaluateMap(context, (Map<?, ?>) sourceObject);
            }
            if (sourceObject instanceof Collection) {
                return evaluateCollection(context, (Collection<?>) sourceObject);
            }
            if (sourceObject.getClass().isArray()) {
                return evaluateArray(context, sourceObject);
            }
            throw new IllegalArgumentException("Unsupported range source: " + sourceObject + ", " + sourceObject.getClass());
        } catch (RuntimeException e) {
            throw failedExecute(e);
        }
    }

    private List<Object> evaluateArray(Context context, Object sourceObject) {
        var length = Array.getLength(sourceObject);
        var out = new ArrayList<>(length);
        var iteration = new HashMap<String, Object>(2, 1);
        for (var i = 0; i < length; i++) {
            var rawItem = Array.get(sourceObject, i);
            iteration.put(firstVariableName, rawItem);
            iteration.put(secondVariableName, i);
            try {
                context.in(iteration);
                var item = body.apply(context);
                out.add(item);
            } finally {
                context.out();
            }
        }
        return out;
    }

    private List<Object> evaluateCollection(Context context, Collection<?> sourceObject) {
        var out = new ArrayList<>(sourceObject.size());
        var iteration = new HashMap<String, Object>(2, 1);
        var index = 0;
        for (var o : sourceObject) {
            iteration.put(firstVariableName, o);
            iteration.put(secondVariableName, index++);
            try {
                context.in(iteration);
                var item = body.apply(context);
                out.add(item);
            } finally {
                context.out();
            }
        }
        return out;
    }

    private List<Object> evaluateMap(Context context, Map<?, ?> sourceObject) {
        var out = new ArrayList<>(sourceObject.size());
        var iteration = new HashMap<String, Object>(2, 1);
        for (var entry : sourceObject.entrySet()) {
            iteration.put(firstVariableName, entry.getKey());
            iteration.put(secondVariableName, entry.getValue());
            try {
                context.in(iteration);
                var item = body.apply(context);
                out.add(item);
            } finally {
                context.out();
            }
        }
        return out;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

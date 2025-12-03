package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Template expression that iterates over a collection or array and produces a list of results.
 * <p>
 * The source expression is evaluated first and must yield either a
 * {@link java.util.Collection} or an array. For each element, a new temporary
 * scope is pushed into the {@link Context}, exposing iteration variables,
 * and the body expression is evaluated.
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
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class RangeTemplateExpression implements TemplateExpression {
    private final TemplateExpression name;
    private final String itemVariableName;
    private final String indexVariableName;
    private final TemplateExpression source;
    private final TemplateExpression body;

    @Override
    public Object apply(final Context context) {
        var sourceObject = source.apply(context);
        if (sourceObject == null) {
            return null;
        }
        if (sourceObject instanceof Collection) {
            return evaluateRange(context, (Collection<?>) sourceObject);
        }
        if (sourceObject.getClass().isArray()) {
            return evaluateArray(context, sourceObject);
        }
        throw new IllegalArgumentException("Unsupported range source: " + sourceObject);
    }

    private ArrayList<Object> evaluateArray(Context context, Object sourceObject) {
        var length = Array.getLength(sourceObject);
        var out = new ArrayList<>(length);
        var iteration = new HashMap<String, Object>(2, 1);
        for (var i = 0; i < length; i++) {
            var rawItem = Array.get(sourceObject, i);
            iteration.put(itemVariableName, rawItem);
            iteration.put(indexVariableName, i);
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

    private ArrayList<Object> evaluateRange(Context context, Collection<?> sourceObject) {
        var out = new ArrayList<>(sourceObject.size());
        var iteration = new HashMap<String, Object>(2, 1);
        var index = 0;
        for (var o : sourceObject) {
            iteration.put(itemVariableName, o);
            iteration.put(indexVariableName, index++);
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

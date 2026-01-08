package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Template expression that constructs an object by evaluating its elements.
 * <p>
 * Each {@link ObjectElement} is applied in declaration order and may contribute
 * one or more key-value pairs to the resulting object.
 * </p>
 *
 * <p>
 * The resulting map preserves insertion order and is created anew on each
 * evaluation.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class ObjectTemplateExpression implements TemplateExpression {
    private final List<ObjectElement> elements;

    @Override
    public Object apply(final Context context) {
        var out = new LinkedHashMap<String, Object>();
        for (var element : elements) {
            if (element == null) {
                throw new IllegalArgumentException("object element must not be null");
            }
            element.apply(context, out);
        }
        return out;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

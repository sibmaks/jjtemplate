package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Template expression that constructs a list by evaluating its elements.
 * <p>
 * Each {@link ListElement} is applied in declaration order and may contribute
 * zero, one, or multiple values to the resulting list.
 * </p>
 *
 * <p>
 * The resulting list preserves element order and is created anew on each
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
public final class ListTemplateExpression implements TemplateExpression {
    private final List<ListElement> elements;

    @Override
    public Object apply(final Context context) {
        var out = new ArrayList<>(elements.size());
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

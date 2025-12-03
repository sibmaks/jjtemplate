package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 *
 * List element that conditionally contributes a value to the resulting list.
 * <p>
 * The wrapped expression is evaluated against the current {@link Context}.
 * If the result is {@code null}, the element is skipped. Otherwise, the
 * evaluated value is appended to the target list.
 * </p>
 *
 * <p>
 * This element is typically used to implement conditional list construction
 * inside templates.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public class ConditionListElement implements ListElement {
    private final TemplateExpression source;

    @Override
    public void apply(Context context, List<Object> target) {
        var v = source.apply(context);
        if (v == null) {
            return;
        }
        target.add(v);
    }

    @Override
    public <T> T visit(ListElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

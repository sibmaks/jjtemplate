package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * List element that evaluates a dynamic expression and appends its result.
 * <p>
 * The wrapped {@link TemplateExpression} is evaluated against the current
 * {@link Context} and the resulting value is always added to the target list,
 * including {@code null} values.
 * </p>
 *
 * <p>
 * This element represents a non-conditional list entry whose value is resolved
 * at evaluation time.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class DynamicListElement implements ListElement {
    private final TemplateExpression value;

    @Override
    public void apply(Context context, List<Object> target) {
        var v = value.apply(context);
        target.add(v);
    }

    @Override
    public <T> T visit(ListElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

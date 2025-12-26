package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * List element that expands the contents of another value into the target list.
 * <p>
 * The wrapped {@link TemplateExpression} is evaluated and its result is handled as follows:
 * </p>
 * <ul>
 *   <li>{@code null} — nothing is added</li>
 *   <li>{@link java.util.Collection} — all elements are added in iteration order</li>
 *   <li>Array — all array elements are added in index order</li>
 *   <li>Any other value — the value itself is added as a single element</li>
 * </ul>
 *
 * <p>
 * This element implements spread semantics similar to {@code ...} in list literals.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class SpreadListElement implements ListElement {
    private final TemplateExpression source;

    @Override
    public void apply(Context context, List<Object> target) {
        var value = source.apply(context);
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?>) {
            var list = (Collection<?>) value;
            target.addAll(list);
            return;
        }
        if (value.getClass().isArray()) {
            var length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                var item = Array.get(value, i);
                target.add(item);
            }
            return;
        }
        target.add(value);
    }

    @Override
    public <T> T visit(ListElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

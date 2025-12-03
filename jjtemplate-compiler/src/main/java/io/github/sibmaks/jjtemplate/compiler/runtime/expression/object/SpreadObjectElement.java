package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * Object element that expands entries of another object into the target object.
 * <p>
 * The wrapped {@link TemplateExpression} is evaluated and must produce a
 * {@link java.util.Map}. All entries of the evaluated map are copied into
 * the target object. If the result is {@code null}, no entries are added.
 * </p>
 *
 * <p>
 * Keys are converted to strings using {@link String#valueOf(Object)}.
 * If a key already exists in the target object, it is overwritten.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public class SpreadObjectElement implements ObjectElement {
    private final TemplateExpression source;

    @Override
    public void apply(Context context, Map<String, Object> target) {
        var v = source.apply(context);
        if (v == null) {
            return;
        }
        if (!(v instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("spread object expects Map, got: " + v.getClass().getName());
        }
        var map = (Map<?, ?>) v;
        for (var e : map.entrySet()) {
            var key = String.valueOf(e.getKey());
            var value = e.getValue();
            target.put(key, value);
        }
    }

    @Override
    public <T> T visit(ObjectElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

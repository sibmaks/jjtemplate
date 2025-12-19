package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * Object element that contributes a single key-value pair.
 * <p>
 * Both the key and the value are resolved by evaluating their respective
 * {@link TemplateExpression} instances against the current {@link Context}.
 * The evaluated key is converted to a string using {@link String#valueOf(Object)}.
 * </p>
 *
 * <p>
 * This element represents a dynamic object field defined in a template.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class ObjectFieldElement implements ObjectElement {
    private final TemplateExpression key;
    private final TemplateExpression value;

    @Override
    public void apply(Context context, Map<String, Object> target) {
        var evaluatedKey = key.apply(context);
        var keyString = String.valueOf(evaluatedKey);
        var evaluatedValue = value.apply(context);
        target.put(keyString, evaluatedValue);
    }

    @Override
    public <T> T visit(ObjectElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * Object element that contributes a static key-value pair.
 * <p>
 * The key and value are stored as constants and are added to the target object
 * without consulting the evaluation {@link Context}.
 * </p>
 *
 * <p>
 * This element represents a literal object field defined directly in the template.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public class ObjectStaticFieldElement implements ObjectElement {
    private final String key;
    private final Object value;

    @Override
    public void apply(Context context, Map<String, Object> target) {
        target.put(key, value);
    }

    @Override
    public <T> T visit(ObjectElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

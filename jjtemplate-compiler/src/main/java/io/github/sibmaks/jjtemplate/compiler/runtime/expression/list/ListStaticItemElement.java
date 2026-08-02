package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * List element that always appends a static constant value.
 * <p>
 * The stored value is added to the target list as-is and does not depend
 * on the evaluation {@link Context}.
 * </p>
 *
 * <p>
 * This element represents a literal list entry defined directly in the template.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
public final class ListStaticItemElement implements ListElement {
    private final Object value;

    /**
     * Creates a static list element.
     *
     * @param value value appended to the list
     */
    public ListStaticItemElement(Object value) {
        this.value = value;
    }

    @Override
    public void apply(Context context, List<Object> target) {
        target.add(value);
    }

    @Override
    public <T> T visit(ListElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

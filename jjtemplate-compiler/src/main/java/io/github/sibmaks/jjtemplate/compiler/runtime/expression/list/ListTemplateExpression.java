package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.AbstractList;
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
@ToString
@EqualsAndHashCode
public final class ListTemplateExpression implements TemplateExpression {
    private final List<? extends ListElement> elements;

    /**
     * Creates a list expression.
     *
     * @param elements list elements in evaluation order
     */
    public ListTemplateExpression(List<? extends ListElement> elements) {
        this.elements = elements;
    }

    @Override
    public List<Object> apply(final Context context) {
        var out = new ArrayList<>(elements.size());
        for (var element : elements) {
            if (element == null) {
                throw new IllegalArgumentException("object element must not be null");
            }
            element.apply(context, out);
        }
        return out;
    }

    /**
     * Creates a list that evaluates each positional element on first access.
     * <p>
     * Spread and conditional elements can change the number of resulting
     * arguments, so those lists retain eager evaluation semantics.
     * </p>
     *
     * @param context evaluation context
     * @return lazily evaluated positional arguments when possible
     */
    public List<Object> applyLazy(final Context context) {
        for (var element : elements) {
            if (!(element instanceof DynamicListElement)
                    && !(element instanceof ListStaticItemElement)) {
                return apply(context);
            }
        }
        return new LazyArgumentList(elements, context);
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    private static final class LazyArgumentList extends AbstractList<Object> {
        private final List<? extends ListElement> elements;
        private final Context context;
        private final Object[] values;
        private final boolean[] evaluated;

        private LazyArgumentList(List<? extends ListElement> elements, Context context) {
            this.elements = elements;
            this.context = context;
            this.values = new Object[elements.size()];
            this.evaluated = new boolean[elements.size()];
        }

        @Override
        public Object get(int index) {
            if (!evaluated[index]) {
                var valuesAtIndex = new ArrayList<Object>(1);
                elements.get(index).apply(context, valuesAtIndex);
                if (valuesAtIndex.size() != 1) {
                    throw new IllegalStateException("lazy argument must produce exactly one value");
                }
                values[index] = valuesAtIndex.get(0);
                evaluated[index] = true;
            }
            return values[index];
        }

        @Override
        public int size() {
            return elements.size();
        }
    }
}

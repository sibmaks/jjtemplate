package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;

import java.util.List;

/**
 * Represents a single element participating in list template construction.
 * <p>
 * Implementations may add zero, one, or multiple values to the target list
 * depending on their semantics (for example: conditional elements, dynamic
 * expressions, or spread operations).
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface ListElement {
    /**
     * Applies this element to the target list using the given evaluation context.
     * <p>
     * Implementations may choose to append values to the target list,
     * skip insertion, or add multiple values depending on their behavior.
     * </p>
     *
     * @param context evaluation context used to resolve dynamic values
     * @param target  target list being constructed
     */
    void apply(Context context, List<Object> target);

    /**
     * Accepts a visitor to perform element-specific processing.
     *
     * @param visitor visitor instance
     * @param <T>     result type produced by the visitor
     * @return value returned by the visitor
     */
    <T> T visit(ListElementVisitor<T> visitor);
}

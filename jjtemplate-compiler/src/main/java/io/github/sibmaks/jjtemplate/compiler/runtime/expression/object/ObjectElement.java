package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;

import java.util.Map;

/**
 * Represents a single element participating in object template construction.
 * <p>
 * Implementations may contribute one or more key-value pairs to the target map
 * depending on their semantics (for example: static fields, dynamic fields,
 * or spread operations).
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface ObjectElement {
    /**
     * Applies this element to the target object using the given evaluation context.
     * <p>
     * Implementations may add entries to the target map, skip insertion,
     * or insert multiple entries depending on their behavior.
     * </p>
     *
     * @param context evaluation context used to resolve dynamic values
     * @param target  target map being constructed
     */
    void apply(Context context, Map<String, Object> target);

    /**
     * Accepts a visitor to perform element-specific processing.
     *
     * @param visitor visitor instance
     * @param <T>     result type produced by the visitor
     * @return value returned by the visitor
     */
    <T> T visit(ObjectElementVisitor<T> visitor);
}

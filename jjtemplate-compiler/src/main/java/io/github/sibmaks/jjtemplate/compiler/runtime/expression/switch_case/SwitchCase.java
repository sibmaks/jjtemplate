package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;

/**
 * Represents a single case within a switch template expression.
 * <p>
 * Implementations define matching logic against an evaluated switch condition
 * and produce a result when the case is selected.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface SwitchCase {
    /**
     * Determines whether this case matches the evaluated switch condition.
     *
     * @param condition evaluated switch condition
     * @param context   current evaluation context
     * @return {@code true} if this case matches
     */
    boolean matches(Object condition, Context context);

    /**
     * Evaluates and returns the result of this switch case.
     *
     * @param context   current evaluation context
     * @param condition evaluated switch condition
     * @return result value of this case
     */
    Object evaluate(Context context, Object condition);

    /**
     * Accepts a visitor to perform switch-case-specific processing.
     *
     * @param visitor visitor instance
     * @param <T>     result type produced by the visitor
     * @return value returned by the visitor
     */
    <T> T visit(SwitchCaseVisitor<T> visitor);
}

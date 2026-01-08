package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;

import java.util.function.Function;

/**
 * Base interface for all template expression nodes.
 * <p>
 * Expressions support two evaluation models:
 * </p>
 * <ul>
 *   <li>context-based evaluation via {@link TemplateExpression#apply(Object)}}</li>
 *   <li>structural traversal/transformation via a {@link TemplateExpressionVisitor}</li>
 * </ul>
 * <p>
 * Implementations must remain immutable and side-effect free.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface TemplateExpression extends Function<Context, Object> {

    /**
     * Applies the given visitor to this expression, allowing external
     * transformation, evaluation, folding, type inference or analysis.
     *
     * @param visitor visitor instance implementing expression-specific logic
     * @param <T>     result type produced by the visitor
     * @return value returned by the visitor for this expression
     */
    <T> T visit(TemplateExpressionVisitor<T> visitor);

}

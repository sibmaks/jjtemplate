package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;

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
     * Returns the original expression source captured at compile time.
     * <p>
     * Implementations may return {@code null} when the expression was created
     * programmatically or when the original source text is unavailable.
     * </p>
     *
     * @return original expression source or {@code null}
     */
    default String getSourceExpression() {
        return null;
    }

    /**
     * Returns the best available expression text for diagnostics.
     * <p>
     * Prefers the compile-time source text when present and falls back to the
     * object's structural string representation otherwise.
     * </p>
     *
     * @return expression text suitable for error messages
     */
    default String getDiagnosticExpression() {
        var sourceExpression = getSourceExpression();
        if (sourceExpression != null && !sourceExpression.isEmpty()) {
            return sourceExpression;
        }
        return toString();
    }

    /**
     * Wraps an evaluation failure into a {@link TemplateEvalException} bound to
     * this expression.
     * <p>
     * The returned exception stores the original error as its standard
     * {@linkplain Throwable#getCause() cause}, while the message only describes
     * which expression failed.
     * </p>
     *
     * @param cause original evaluation error
     * @return wrapped template evaluation exception
     */
    default TemplateEvalException failedExecute(Throwable cause) {
        return new TemplateEvalException(
                String.format("Failed execute: \"%s\"", getDiagnosticExpression()),
                cause
        );
    }

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

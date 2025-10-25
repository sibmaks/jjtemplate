package io.github.sibmaks.jjtemplate.parser.api;

/**
 * Represents an expression within the template parsing or evaluation process.
 * <p>
 * This interface serves as a common type for all kinds of expressions
 * (e.g., literals, variables, operations) that can appear in a template.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public interface Expression {

    /**
     * Accepts a visitor that can process this expression.
     * <p>
     * This method enables the use of the Visitor pattern for traversing
     * or transforming expression trees.
     * </p>
     *
     * @param visitor the visitor to accept
     * @param <R>     the result type returned by the visitor
     * @return the result of the visitor's processing
     */
    <R> R accept(ExpressionVisitor<R> visitor);

}

package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * Represents a pipe expression within a template.
 * <p>
 * A pipe expression applies a sequence of function calls (the pipe chain)
 * to the result of a left-hand expression, similar to the pipe operator in functional programming.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode
public class PipeExpression implements Expression {
    /**
     * The left-hand expression whose result is passed through the pipe chain.
     */
    public final Expression left;
    /**
     * The sequence of function calls applied to the left-hand expression.
     */
    public final List<FunctionCallExpression> chain;

    /**
     * Creates a new {@code PipeExpression} instance.
     *
     * @param left  the input expression to be processed by the pipe chain
     * @param chain the list of function calls forming the pipe chain
     */
    public PipeExpression(Expression left, List<FunctionCallExpression> chain) {
        this.left = left;
        this.chain = chain;
    }
}

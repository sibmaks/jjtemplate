package io.github.sibmaks.jjtemplate.parser.api;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class PipeExpression implements Expression {
    /**
     * The left-hand expression whose result is passed through the pipe chain.
     */
    public final Expression left;
    /**
     * The sequence of function calls applied to the left-hand expression.
     */
    public final List<FunctionCallExpression> chain;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitPipe(this);
    }
}

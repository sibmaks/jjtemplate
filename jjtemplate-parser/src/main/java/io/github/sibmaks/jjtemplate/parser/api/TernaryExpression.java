package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a ternary conditional expression within a template.
 * <p>
 * Evaluates the {@code condition} expression and returns either {@code ifTrue}
 * or {@code ifFalse} depending on the result.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode
public final class TernaryExpression implements Expression {
    /**
     * The condition expression to evaluate.
     */
    public final Expression condition;

    /**
     * The expression to evaluate if the condition is {@code true}.
     */
    public final Expression ifTrue;

    /**
     * The expression to evaluate if the condition is {@code false}.
     */
    public final Expression ifFalse;

    /**
     * Creates a ternary expression.
     *
     * @param condition condition to evaluate
     * @param ifTrue expression selected for a true condition
     * @param ifFalse expression selected for a false condition
     */
    public TernaryExpression(Expression condition, Expression ifTrue, Expression ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitTernary(this);
    }
}

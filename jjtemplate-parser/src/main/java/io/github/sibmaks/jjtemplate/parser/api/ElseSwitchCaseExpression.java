package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents an "else" switch case expression within a template.
 * <p>
 * The condition expression may be {@code null} when the case is unconditional.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@ToString
@EqualsAndHashCode
public final class ElseSwitchCaseExpression implements Expression {
    /**
     * Optional condition expression.
     */
    public final Expression condition;

    /**
     * Creates an else-case expression.
     *
     * @param condition optional condition expression
     */
    public ElseSwitchCaseExpression(Expression condition) {
        this.condition = condition;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitElseSwitchCase(this);
    }
}

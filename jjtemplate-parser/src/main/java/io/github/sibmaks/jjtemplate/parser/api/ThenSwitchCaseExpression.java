package io.github.sibmaks.jjtemplate.parser.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a "then" switch case expression within a template.
 * <p>
 * The condition expression may be {@code null} when the case is unconditional.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class ThenSwitchCaseExpression implements Expression {
    /**
     * Optional condition expression.
     */
    public final Expression condition;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitThenSwitchCase(this);
    }
}

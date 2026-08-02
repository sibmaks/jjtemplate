package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Function argument spread expression
 *
 * @author sibmaks
 * @since 0.5.0
 */
@ToString
@EqualsAndHashCode
public final class SpreadExpression implements Expression {
    /**
     * Spreading expression.
     */
    public final Expression source;

    /**
     * Creates a spread expression.
     *
     * @param source expression whose value is spread
     */
    public SpreadExpression(Expression source) {
        this.source = source;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitSpread(this);
    }
}

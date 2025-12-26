package io.github.sibmaks.jjtemplate.parser.api;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public final class SpreadExpression implements Expression {
    /**
     * Spreading expression.
     */
    public final Expression source;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitSpread(this);
    }
}

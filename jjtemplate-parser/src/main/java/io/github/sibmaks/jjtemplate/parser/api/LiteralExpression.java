package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a literal value within a template expression.
 * <p>
 * A literal can be any constant value such as a string, number, or boolean.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode
public final class LiteralExpression implements Expression {
    /**
     * The literal value of this expression.
     */
    public final Object value;

    /**
     * Creates a literal expression.
     *
     * @param value literal value
     */
    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLiteral(this);
    }
}

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
public class LiteralExpression implements Expression {
    /**
     * The literal value of this expression.
     */
    public final Object value;

    /**
     * Creates a new {@code LiteralExpression} instance.
     *
     * @param value the literal value represented by this expression
     */
    public LiteralExpression(Object value) {
        this.value = value;
    }
}

package io.github.sibmaks.jjtemplate.parser.api;

/**
 *
 * @author sibmaks
 */
public class LiteralExpression implements Expression {
    public final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}

package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author sibmaks
 */
@ToString
@EqualsAndHashCode
public class LiteralExpression implements Expression {
    public final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }
}

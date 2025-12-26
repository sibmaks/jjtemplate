package io.github.sibmaks.jjtemplate.parser.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a switch expression within a template.
 * <p>
 * Switch expressions provide a key and a condition expression that is
 * evaluated against the key to choose a branch.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class SwitchExpression implements Expression {
    /**
     * The key expression used by the switch.
     */
    public final Expression key;
    /**
     * The condition expression to evaluate.
     */
    public final Expression condition;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitSwitch(this);
    }
}

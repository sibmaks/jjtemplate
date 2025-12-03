package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Template expression representing a constant literal value.
 * <p>
 * This expression does not depend on the evaluation context and always
 * returns the same value it was constructed with. It is commonly used
 * as the result of constant folding or as the terminal value in expression
 * evaluation.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class ConstantTemplateExpression implements TemplateExpression {
    private final Object value;

    @Override
    public Object apply(final Context context) {
        return value;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Switch case that matches against a constant value.
 * <p>
 * The case is selected when the evaluated switch condition is equal to the
 * stored constant according to {@link Object#equals(Object)} semantics.
 * A {@code null} constant matches only a {@code null} condition.
 * </p>
 *
 * <p>
 * When matched, the associated value expression is evaluated and returned.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class ConstantSwitchCase implements SwitchCase {
    private final Object constant;
    private final TemplateExpression value;

    @Override
    public boolean matches(Object condition, Context context) {
        if (constant == null) {
            return condition == null;
        }
        return constant.equals(condition);
    }

    @Override
    public Object evaluate(Context context, Object condition) {
        return value.apply(context);
    }

    @Override
    public <T> T visit(SwitchCaseVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;

/**
 * Switch case that matches using a dynamically evaluated key expression.
 * <p>
 * The key expression is evaluated against the current {@link Context} and
 * compared with the evaluated switch condition using
 * {@link java.util.Objects#equals(Object, Object)} semantics.
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
public class ExpressionSwitchCase implements SwitchCase {
    private final TemplateExpression key;
    private final TemplateExpression value;

    @Override
    public boolean matches(Object condition, Context context) {
        var keyValue = key.apply(context);
        return Objects.equals(keyValue, condition);
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

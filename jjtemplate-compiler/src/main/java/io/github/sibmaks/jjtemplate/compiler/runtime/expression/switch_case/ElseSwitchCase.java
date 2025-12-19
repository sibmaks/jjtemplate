package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.*;

/**
 * Switch case representing an unconditional {@code else} branch.
 * <p>
 * This case always matches and is typically used as the fallback branch in
 * a switch expression. When selected, the wrapped value expression is evaluated
 * and returned.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class ElseSwitchCase implements SwitchCase {
    private final TemplateExpression value;

    @Override
    public boolean matches(Object condition, Context context) {
        return true;
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

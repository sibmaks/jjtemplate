package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import lombok.*;

/**
 * Compile-time only structure.
 * Must not participate in runtime evaluation or folding.
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class SwitchDefinitionTemplateExpression implements TemplateExpression {
    private final TemplateExpression key;
    private final TemplateExpression condition;

    @Override
    public Object apply(final Context context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        throw new UnsupportedOperationException();
    }
}

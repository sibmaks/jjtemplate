package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import lombok.*;

import java.util.List;

/**
 * Template expression that selects and evaluates a value based on switch cases.
 * <p>
 * The switch condition is evaluated first and then matched against the
 * configured {@link SwitchCase} instances in declaration order. The first
 * matching case is selected and evaluated.
 * </p>
 *
 * <p>
 * If no case matches, the expression evaluates to {@code null}.
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
public final class SwitchTemplateExpression implements TemplateExpression {
    private final TemplateExpression switchKey;
    private final TemplateExpression condition;
    private final List<SwitchCase> cases;

    @Override
    public Object apply(final Context context) {
        var conditionValue = condition.apply(context);

        for (var switchCase : cases) {
            if (switchCase == null) {
                throw new IllegalArgumentException("switch case must not be null");
            }
            if (switchCase.matches(conditionValue, context)) {
                return switchCase.evaluate(context, conditionValue);
            }
        }

        return null;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

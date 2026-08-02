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
@ToString
@EqualsAndHashCode
public final class SwitchTemplateExpression implements TemplateExpression {
    private final TemplateExpression condition;
    private final List<SwitchCase> cases;
    private final String sourceExpression;

    /**
     * Creates a runtime switch expression.
     *
     * @param condition switch condition
     * @param cases cases in matching order
     * @param sourceExpression original source expression
     */
    public SwitchTemplateExpression(
            TemplateExpression condition,
            List<SwitchCase> cases,
            String sourceExpression
    ) {
        this.condition = condition;
        this.cases = cases;
        this.sourceExpression = sourceExpression;
    }

    @Override
    public Object apply(final Context context) {
        try {
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
        } catch (RuntimeException e) {
            throw failedExecute(e);
        }
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

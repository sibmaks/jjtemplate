package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a ternary conditional expression of the form
 * <code>condition ? thenTrue : thenFalse</code>.
 * <p>
 * During evaluation, the condition is resolved first; its result must be
 * a boolean value. Depending on this result, either the <em>true</em> branch
 * or the <em>false</em> branch is evaluated.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class TernaryTemplateExpression implements TemplateExpression {
    private final TemplateExpression condition;
    private final TemplateExpression thenTrue;
    private final TemplateExpression thenFalse;

    @Override
    public Object apply(final Context context) {
        var evaluatedCondition = evaluateCondition(context);
        if (evaluatedCondition) {
            return thenTrue.apply(context);
        }
        return thenFalse.apply(context);
    }

    /**
     * Evaluate condition in specific context
     *
     * @param context current evaluation context
     * @return evaluated condition value
     */
    public boolean evaluateCondition(Context context) {
        var evaluatedCondition = condition.apply(context);
        if (!(evaluatedCondition instanceof Boolean)) {
            throw new IllegalStateException("Cannot evaluate expression: " + this + ", condition is not boolean: " + evaluatedCondition);
        }
        return (boolean) evaluatedCondition;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

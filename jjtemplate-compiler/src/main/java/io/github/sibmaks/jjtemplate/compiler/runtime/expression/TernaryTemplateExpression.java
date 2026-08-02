package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
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
@ToString
@EqualsAndHashCode
public final class TernaryTemplateExpression implements TemplateExpression {
    private final TemplateExpression condition;
    private final TemplateExpression thenTrue;
    private final TemplateExpression thenFalse;
    private final String sourceExpression;

    /**
     * Creates a ternary expression.
     *
     * @param condition condition expression
     * @param thenTrue true branch
     * @param thenFalse false branch
     * @param sourceExpression original source expression
     */
    public TernaryTemplateExpression(
            TemplateExpression condition,
            TemplateExpression thenTrue,
            TemplateExpression thenFalse,
            String sourceExpression
    ) {
        this.condition = condition;
        this.thenTrue = thenTrue;
        this.thenFalse = thenFalse;
        this.sourceExpression = sourceExpression;
    }

    @Override
    public Object apply(final Context context) {
        try {
            var evaluatedCondition = evaluateCondition(context);
            if (evaluatedCondition) {
                return thenTrue.apply(context);
            }
            return thenFalse.apply(context);
        } catch (RuntimeException e) {
            throw failedExecute(e);
        }
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
            throw new IllegalStateException(
                    "Cannot evaluate expression: " + getDiagnosticExpression() + ", condition is not boolean: " + evaluatedCondition
            );
        }
        return (boolean) evaluatedCondition;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

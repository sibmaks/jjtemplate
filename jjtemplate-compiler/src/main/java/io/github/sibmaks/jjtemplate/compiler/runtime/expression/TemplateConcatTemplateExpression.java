package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Represents a concatenation of multiple template expressions.
 * <p>
 * Each child expression is evaluated and converted to its string form,
 * and the results are concatenated in-order. This is the fundamental
 * structure behind template literal building.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class TemplateConcatTemplateExpression implements TemplateExpression {
    private final List<TemplateExpression> expressions;

    @Override
    public String apply(final Context context) {
        var result = new StringBuilder();
        for (var expression : expressions) {
            var evaluated = expression.apply(context);
            result.append(evaluated);
        }
        return result.toString();
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

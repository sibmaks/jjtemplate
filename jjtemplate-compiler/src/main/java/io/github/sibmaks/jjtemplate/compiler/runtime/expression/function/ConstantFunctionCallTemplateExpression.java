package io.github.sibmaks.jjtemplate.compiler.runtime.expression.function;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Function call with static arguments. Used for dynamic functions.
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class ConstantFunctionCallTemplateExpression implements FunctionCallTemplateExpression {
    private final TemplateFunction<?> function;
    private final List<Object> arguments;

    @Override
    public List<Object> getArguments(Context context) {
        return arguments;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

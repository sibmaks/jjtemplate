package io.github.sibmaks.jjtemplate.compiler.runtime.expression.function;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Function call with dynamic arguments. Arguments evaluated on render time.
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class DynamicFunctionCallTemplateExpression implements FunctionCallTemplateExpression {
    private final TemplateFunction<?> function;
    private final ListTemplateExpression argExpression;

    @Override
    public List<Object> getArguments(Context context) {
        return new ArrayList<>(argExpression.apply(context));
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

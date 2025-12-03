package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a function call inside a template expression.
 * <p>
 * This expression evaluates each argument, collects their values
 * and invokes the underlying {@link TemplateFunction}. It may also
 * serve as a step in a pipe chain, receiving an additional input value.
 * </p>
 * <p>
 * Instances of this class are immutable and may participate in
 * compile-time folding or variable substitution performed by visitors.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class FunctionCallTemplateExpression implements TemplateExpression {
    private final TemplateFunction<?> function;
    private final List<TemplateExpression> argExpressions;

    @Override
    public Object apply(final Context context) {
        var args = argExpressions.stream()
                .map(it -> it.apply(context))
                .collect(Collectors.toList());
        return function.invoke(args);
    }

    /**
     * Evaluates this function call as a pipe operation.
     * <p>
     * All argument expressions are evaluated against the provided context,
     * and the resulting values are passed to the function along with
     * the supplied pipe value.
     * </p>
     *
     * @param context evaluation context for resolving variables
     * @param pipe    value coming from the left side of a pipe chain
     * @return result of invoking the function with evaluated arguments and pipe value
     */
    public Object apply(final Context context, final Object pipe) {
        var args = argExpressions.stream()
                .map(it -> it.apply(context))
                .collect(Collectors.toList());
        return function.invoke(args, pipe);
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

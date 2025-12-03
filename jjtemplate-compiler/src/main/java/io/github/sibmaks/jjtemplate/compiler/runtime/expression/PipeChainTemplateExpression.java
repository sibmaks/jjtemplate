package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Represents a pipe-style expression chain where the output of one
 * expression becomes the input to subsequent function calls.
 * <p>
 * Evaluates the root expression first, then sequentially applies each
 * function from the chain using the previous result as a pipe argument.
 * </p>
 *
 * <p>
 * This enables expressions such as:
 * <pre>{@code
 *   value | func1 a, b | func2 | func3 x
 * }</pre>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class PipeChainTemplateExpression implements TemplateExpression {
    private final TemplateExpression root;
    private final List<FunctionCallTemplateExpression> chain;

    @Override
    public Object apply(final Context context) {
        var value = root.apply(context);
        for (var pipe : chain) {
            value = pipe.apply(context, value);
        }
        return value;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

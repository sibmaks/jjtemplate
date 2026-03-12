package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.parser.api.*;

import java.util.stream.Collectors;

/**
 * Renders parsed expressions back into a compact source-like representation.
 *
 * @author sibmaks
 * @since 0.8.0
 */
public final class ExpressionSourceRenderer implements ExpressionVisitor<String> {

    /**
     * Render expression to source.
     *
     * @param expression source expression
     * @return rendered source
     */
    public String render(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitLiteral(LiteralExpression expr) {
        var value = expr.value;
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            var stringValue = (String) value;
            return "'" + stringValue
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    + "'";
        }
        return String.valueOf(value);
    }

    @Override
    public String visitVariable(VariableExpression expr) {
        if (expr.segments.isEmpty()) {
            return ".";
        }
        var builder = new StringBuilder();
        for (var segment : expr.segments) {
            builder.append('.').append(segment.name);
            if (segment.isMethod()) {
                builder.append('(')
                        .append(segment.args.stream().map(this::render).collect(Collectors.joining(", ")))
                        .append(')');
            }
        }
        return builder.toString();
    }

    @Override
    public String visitFunction(FunctionCallExpression expr) {
        var builder = new StringBuilder();
        if (expr.namespace != null && !expr.namespace.isEmpty()) {
            builder.append(expr.namespace).append(':');
        }
        builder.append(expr.name);
        if (!expr.args.isEmpty()) {
            builder.append(' ')
                    .append(expr.args.stream().map(this::render).collect(Collectors.joining(", ")));
        }
        return builder.toString();
    }

    @Override
    public String visitPipe(PipeExpression expr) {
        return render(expr.left) + " | " + expr.chain.stream()
                .map(this::render)
                .collect(Collectors.joining(" | "));
    }

    @Override
    public String visitTernary(TernaryExpression expr) {
        return render(expr.condition) + " ? " + render(expr.ifTrue) + " : " + render(expr.ifFalse);
    }

    @Override
    public String visitSwitch(SwitchExpression expr) {
        return render(expr.key) + " switch " + render(expr.condition);
    }

    @Override
    public String visitRange(RangeExpression expr) {
        return render(expr.name) + " range " + expr.itemVariableName + "," + expr.indexVariableName + " of "
                + render(expr.source);
    }

    @Override
    public String visitThenSwitchCase(ThenSwitchCaseExpression expr) {
        if (expr.condition == null) {
            return "then";
        }
        return "then " + render(expr.condition);
    }

    @Override
    public String visitElseSwitchCase(ElseSwitchCaseExpression expr) {
        if (expr.condition == null) {
            return "else";
        }
        return "else " + render(expr.condition);
    }

    @Override
    public String visitSpread(SpreadExpression spreadExpression) {
        return "..." + render(spreadExpression.source);
    }
}

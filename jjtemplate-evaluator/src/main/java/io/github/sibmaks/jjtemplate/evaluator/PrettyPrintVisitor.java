package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.parser.api.*;

import java.util.stream.Collectors;

/**
 * Pretty-printing visitor that converts expressions into a readable
 * string representation similar to the original template syntax.
 *
 * <p>Used primarily for debugging, logging, and error reporting.</p>
 * <p>
 * Supports:
 * <ul>
 *   <li>Literals</li>
 *   <li>Variable access chains</li>
 *   <li>Function calls with optional namespaces</li>
 *   <li>Pipe expressions</li>
 *   <li>Ternary expressions</li>
 * </ul>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public class PrettyPrintVisitor implements ExpressionVisitor<String> {

    @Override
    public String visitLiteral(LiteralExpression expr) {
        var value = expr.value;
        if (value instanceof CharSequence) {
            return String.format("'%s'", value);
        }
        return String.format("%s", value);
    }

    @Override
    public String visitVariable(VariableExpression expr) {
        var builder = new StringBuilder(".");
        var segments = expr.segments;
        for (int i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);
            builder.append(segment.name);
            if (segment.isMethod()) {
                builder.append('(');
                var args = segment.args;
                for (int argIndex = 0; argIndex < args.size(); argIndex++) {
                    var arg = args.get(argIndex);
                    builder.append(arg.accept(this));
                    if (argIndex < args.size() - 1) {
                        builder.append(", ");
                    }
                }
                builder.append(')');
            }
            if (i < segments.size() - 1) {
                builder.append('.');
            }
        }
        return builder
                .toString();
    }

    @Override
    public String visitFunction(FunctionCallExpression expr) {
        var builder = new StringBuilder();
        var args = expr.args.stream()
                .map(it -> it.accept(this))
                .collect(Collectors.joining(", "));
        if (expr.namespace.isEmpty()) {
            builder.append(expr.name);
        } else {
            builder
                    .append(expr.namespace)
                    .append(':')
                    .append(expr.name);
        }
        if (!args.isEmpty()) {
            builder.insert(0, '(')
                    .append(' ')
                    .append(args)
                    .append(')');
        }
        return builder
                .toString();
    }

    @Override
    public String visitPipe(PipeExpression expr) {
        return String.format(
                "(%s | %s)",
                expr.left.accept(this),
                expr.chain.stream()
                        .map(it -> String.format("%s", it.accept(this)))
                        .collect(Collectors.joining(" | "))
        );
    }

    @Override
    public String visitTernary(TernaryExpression expr) {
        return String.format(
                "(%s ? %s : %s)",
                expr.condition.accept(this),
                expr.ifTrue.accept(this),
                expr.ifFalse.accept(this)
        );
    }
}

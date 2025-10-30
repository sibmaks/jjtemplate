package io.github.sibmaks.jjtemplate.compiler.visitor;

import io.github.sibmaks.jjtemplate.compiler.Nodes;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.parser.api.*;
import lombok.AllArgsConstructor;

/**
 * Converts parsed {@link Expression} trees into executable {@link AstNode} structures.
 * <p>
 * This visitor evaluates constant expressions where possible and wraps others
 * in appropriate AST node types for later execution.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor
public final class AstTreeConvertVisitor implements ExpressionVisitor<AstNode> {
    /**
     * Evaluator used for partial expression evaluation during AST conversion.
     */
    private final TemplateEvaluator evaluator;

    private static Expression getExpression(AstNode node) {
        if (node instanceof Nodes.ExpressionNode) {
            return ((Nodes.ExpressionNode) node).getExpression();
        } else if (node instanceof Nodes.StaticNode) {
            return new LiteralExpression(((Nodes.StaticNode) node).getValue());
        }
        throw new IllegalArgumentException("unexpected node type: " + node.getClass());
    }

    @Override
    public AstNode visitLiteral(LiteralExpression expr) {
        return Nodes.StaticNode.of(expr.value);
    }

    @Override
    public AstNode visitVariable(VariableExpression expr) {
        return new Nodes.ExpressionNode(expr);
    }

    @Override
    public AstNode visitFunction(FunctionCallExpression expr) {
        if (!expr.args.stream().
                allMatch(LiteralExpression.class::isInstance)) {
            return new Nodes.ExpressionNode(expr);
        }
        return foldExpression(expr);
    }

    private AstNode foldExpression(Expression expr) {
        try {
            var value = evaluator.evaluate(expr, Context.empty());
            return Nodes.StaticNode.of(value);
        } catch (Exception ignored) {
            // ignore folding exceptions
        }
        return new Nodes.ExpressionNode(expr);
    }

    @Override
    public AstNode visitPipe(PipeExpression expr) {
        if (!(expr.left instanceof LiteralExpression) ||
                !expr.chain.stream()
                        .flatMap(c -> c.args.stream()).allMatch(LiteralExpression.class::isInstance)) {
            return new Nodes.ExpressionNode(expr);
        }
        return foldExpression(expr);
    }

    @Override
    public AstNode visitTernary(TernaryExpression expr) {
        var c = expr.condition.accept(this);
        var t = expr.ifTrue.accept(this);
        var f = expr.ifFalse.accept(this);
        if (c instanceof Nodes.StaticNode) {
            var staticNode = (Nodes.StaticNode) c;
            var condValue = staticNode.getValue();
            if (!(condValue instanceof Boolean)) {
                throw new IllegalArgumentException("ternaryOperator condition is not a boolean");
            }
            if ((boolean) condValue) {
                return t;
            } else {
                return f;
            }
        }
        if (!(c instanceof Nodes.ExpressionNode)) {
            throw new IllegalStateException("ternaryOperator condition is not an expression: " + c.getClass());
        }
        var ce = (Nodes.ExpressionNode) c;
        return new Nodes.ExpressionNode(
                new TernaryExpression(
                        ce.getExpression(),
                        getExpression(t),
                        getExpression(f)
                )
        );
    }
}

package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.Nodes;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstVisitor;
import io.github.sibmaks.jjtemplate.parser.api.Expression;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects variable references from an abstract syntax tree (AST).
 * <p>
 * This visitor traverses the AST and accumulates all variable names
 * referenced within expressions, conditionals, ranges, and objects.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
final class AstVarRefCollector implements AstVisitor<Void> {
    /**
     * Accumulator set for collected variable names.
     */
    private final Set<String> accumulator = new LinkedHashSet<>();

    /**
     * Traverses the given AST node and returns a set of all variable names
     * referenced within it.
     *
     * @param node the AST node to analyze
     * @return a set of variable names used in the node
     */
    static Set<String> collect(AstNode node) {
        if (node == null) {
            return Collections.emptySet();
        }
        var c = new AstVarRefCollector();
        node.accept(c);
        return c.accumulator;
    }

    @Override
    public Void visitStatic(Nodes.StaticNode node) {
        return null;
    }

    @Override
    public Void visitExpression(Nodes.ExpressionNode node) {
        return visitRefNode(node.getExpression());
    }

    @Override
    public Void visitSpread(Nodes.SpreadNode node) {
        return visitRefNode(node.getExpression());
    }

    @Override
    public Void visitCond(Nodes.CondNode node) {
        return visitRefNode(node.getExpression());
    }

    private Void visitRefNode(Expression node) {
        node.accept(new ExpressionVarRefCollector(accumulator));
        return null;
    }

    @Override
    public Void visitObject(Nodes.CompiledObject node) {
        for (var e : node.getEntries()) {
            if (e instanceof Nodes.CompiledObject.Field) {
                var field = (Nodes.CompiledObject.Field) e;
                var fieldKey = field.getKey();
                fieldKey.accept(this);
                var fieldValue = field.getValue();
                fieldValue.accept(this);
            } else if (e instanceof Nodes.CompiledObject.Spread) {
                var spread = (Nodes.CompiledObject.Spread) e;
                var expression = spread.getExpression();
                expression.accept(new ExpressionVarRefCollector(accumulator));
            }
            // static field hasn't references
        }
        return null;
    }

    @Override
    public Void visitSwitch(Nodes.SwitchDefinition node) {
        var switchExpr = node.getSwitchExpr();
        switchExpr.accept(new ExpressionVarRefCollector(accumulator));
        var branches = node.getBranches();
        for (var key : branches.keySet()) {
            key.accept(new ExpressionVarRefCollector(accumulator));
        }
        for (var val : branches.values()) {
            val.accept(this);
        }
        var thenNode = node.getThenNode();
        if (thenNode != null) {
            thenNode.accept(this);
        }
        var elseNode = node.getElseNode();
        if (elseNode != null) {
            elseNode.accept(this);
        }
        return null;
    }

    @Override
    public Void visitRange(Nodes.RangeDefinition node) {
        var sourceExpr = node.getSourceExpr();
        sourceExpr.accept(new ExpressionVarRefCollector(accumulator));
        var bodyNode = node.getBodyNode();
        bodyNode.accept(this);
        return null;
    }

    @Override
    public Void visitList(Nodes.ListNode node) {
        var astNodes = node.getAstNodes();
        for (var it : astNodes) {
            it.accept(this);
        }
        return null;
    }

    @Override
    public Void visitDefault(Object node) {
        return null;
    }
}

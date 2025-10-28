package io.github.sibmaks.jjtemplate.compiler.visitor.ast;

import io.github.sibmaks.jjtemplate.compiler.Nodes;

import java.util.List;

/**
 * Visitor interface for traversing and processing abstract syntax tree (AST) nodes.
 * <p>
 * Implementations of this interface can perform transformations, evaluations,
 * or optimizations on different kinds of AST nodes.
 * </p>
 *
 * @param <R> the result type produced by the visitor
 * @author sibmaks
 * @since 0.0.1
 */
public interface AstVisitor<R> {
    /**
     * Visits a {@link Nodes.StaticNode}.
     *
     * @param node the static node to visit
     * @return the result of visiting the node
     */
    R visitStatic(Nodes.StaticNode node);

    /**
     * Visits a {@link Nodes.SwitchDefinition}.
     *
     * @param node the switch definition node to visit
     * @return the result of visiting the node
     */
    R visitSwitch(Nodes.SwitchDefinition node);

    /**
     * Visits a {@link Nodes.RangeDefinition}.
     *
     * @param node the range definition node to visit
     * @return the result of visiting the node
     */
    R visitRange(Nodes.RangeDefinition node);

    /**
     * Visits a {@link Nodes.CompiledObject}.
     *
     * @param node the compiled object node to visit
     * @return the result of visiting the node
     */
    R visitObject(Nodes.CompiledObject node);

    /**
     * Visits a {@link Nodes.CondNode}.
     *
     * @param node the conditional node to visit
     * @return the result of visiting the node
     */
    R visitCond(Nodes.CondNode node);

    /**
     * Visits a {@link Nodes.SpreadNode}.
     *
     * @param node the spread node to visit
     * @return the result of visiting the node
     */
    R visitSpread(Nodes.SpreadNode node);

    /**
     * Visits a {@link Nodes.ExpressionNode}.
     *
     * @param node the expression node to visit
     * @return the result of visiting the node
     */
    R visitExpression(Nodes.ExpressionNode node);

    /**
     * Visits a list of AST nodes.
     *
     * @param node the list of AST nodes
     * @return the result of visiting the list
     */
    R visitList(List<AstNode> node);

    /**
     * Default visitor method called when no specific handler matches the node type.
     *
     * @param node the node or object to visit
     * @return the result of the fallback visit
     */
    R visitDefault(Object node);
}

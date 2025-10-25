package io.github.sibmaks.jjtemplate.compiler.visitor.ast;

/**
 * Represents a node in the abstract syntax tree (AST) of a compiled template.
 * <p>
 * Each node type implements this interface and participates in the visitor pattern
 * to allow traversal, transformation, and evaluation.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public interface AstNode {
    /**
     * Accepts a visitor to process this AST node.
     *
     * @param visitor the visitor to accept
     * @param <R>     the result type returned by the visitor
     * @return the result of visiting this node
     */
    <R> R accept(AstVisitor<R> visitor);

}

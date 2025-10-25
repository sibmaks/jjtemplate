package io.github.sibmaks.jjtemplate.compiler.visitor.ast;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for safely dispatching AST nodes to their corresponding visitors.
 * <p>
 * Provides a helper method to invoke the appropriate {@link AstVisitor} method
 * depending on whether the given object implements {@link AstNode}.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AstVisitorUtils {
    /**
     * Dispatches the given node to the appropriate {@link AstVisitor} method.
     * <p>
     * If the node implements {@link AstNode}, its {@code accept} method is called;
     * otherwise, the visitor's {@code visitDefault} method is used.
     * </p>
     *
     * @param node    the object or AST node to visit
     * @param visitor the visitor handling the node
     * @param <R>     the result type returned by the visitor
     * @return the result of the visitor invocation
     */
    public static <R> R dispatch(Object node, AstVisitor<R> visitor) {
        if (node instanceof AstNode) {
            var ast = (AstNode) node;
            return ast.accept(visitor);
        }
        return visitor.visitDefault(node);
    }

}

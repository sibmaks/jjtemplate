package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of an optimization or compilation pass.
 * <p>
 * Contains the optimized or compiled template along with its associated
 * definition maps.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor
public final class Result {
    /**
     * The list of compiled or optimized definition maps.
     */
    private final List<Map<String, AstNode>> definitions;

    /**
     * The root node of the optimized or compiled template AST.
     */
    private final AstNode template;

}
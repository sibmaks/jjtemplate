package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.InternalVariable;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents the result of an optimization or compilation pass.
 * <p>
 * Contains the optimized or compiled template along with its associated
 * internal variables.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor
public final class Result {
    /**
     * The list of compiled or optimized internal variables.
     */
    private final List<InternalVariable> internalVariables;

    /**
     * The root node of the optimized or compiled template AST.
     */
    private final AstNode template;

}
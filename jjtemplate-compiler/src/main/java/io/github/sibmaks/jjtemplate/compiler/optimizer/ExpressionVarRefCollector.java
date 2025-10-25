package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.parser.api.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * Collects variable references from an {@link Expression} tree.
 * <p>
 * This visitor traverses the expression structure and records the names
 * of all root-level variables encountered.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ExpressionVarRefCollector implements ExpressionVisitor<Void> {
    /**
     * The set in which discovered variable names are accumulated.
     */
    private final Set<String> accumulator;

    @Override
    public Void visitLiteral(LiteralExpression expr) {
        return null;
    }

    @Override
    public Void visitVariable(VariableExpression expr) {
        var root = expr.segments.get(0).name;
        accumulator.add(root);
        return null;
    }

    @Override
    public Void visitFunction(FunctionCallExpression expr) {
        for (var a : expr.args) {
            a.accept(this);
        }
        return null;
    }

    @Override
    public Void visitPipe(PipeExpression expr) {
        expr.left.accept(this);
        for (var c : expr.chain) {
            for (var a : c.args) {
                a.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitTernary(TernaryExpression expr) {
        expr.condition.accept(this);
        expr.ifTrue.accept(this);
        expr.ifFalse.accept(this);
        return null;
    }
}
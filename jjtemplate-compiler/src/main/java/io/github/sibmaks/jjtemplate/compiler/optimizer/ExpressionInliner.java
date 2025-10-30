package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.parser.api.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Map;

/**
 * Inlines constant references within expression trees.
 * <p>
 * This visitor replaces variable references with their constant literal values
 * when available and evaluates expressions composed entirely of literals.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ExpressionInliner implements ExpressionVisitor<Expression> {
    /**
     * Map of constant variable bindings (name → value) used for inlining.
     */
    private final Map<String, Object> constants;

    /**
     * Evaluator used for partial expression evaluation when all arguments are literals.
     */
    private final TemplateEvaluator evaluator;


    @Override
    public Expression visitLiteral(LiteralExpression expr) {
        return expr;
    }

    @Override
    public Expression visitVariable(VariableExpression expr) {
        if (expr.segments.size() == 1) {
            var seg = expr.segments.get(0);
            if (!seg.isMethod() && constants.containsKey(seg.name)) {
                Object v = constants.get(seg.name);
                return new LiteralExpression(v);
            }
        }
        return expr;
    }

    @Override
    public Expression visitFunction(FunctionCallExpression expr) {
        var changed = false;
        var args = new ArrayList<Expression>(expr.args.size());
        for (var arg : expr.args) {
            var inlinedArg = arg.accept(this);
            args.add(inlinedArg);
            changed |= (inlinedArg != arg);
        }

        var updated = changed
                ? new FunctionCallExpression(expr.name, args)
                : expr;

        if (updated.args.stream().allMatch(LiteralExpression.class::isInstance)) {
            try {
                var result = evaluator.evaluate(updated, Context.empty());
                return new LiteralExpression(result);
            } catch (Exception ignored) {
                // ignore inlining exceptions
            }
        }

        return updated;
    }

    @Override
    public Expression visitPipe(PipeExpression expr) {
        var lhe = expr.left.accept(this);
        var changed = (lhe != expr.left);
        var rhe = new ArrayList<FunctionCallExpression>(expr.chain.size());
        for (var c : expr.chain) {
            var rheChanged = false;
            var args2 = new ArrayList<Expression>(c.args.size());
            for (var arg : c.args) {
                var inlinedArg = arg.accept(this);
                args2.add(inlinedArg);
                rheChanged |= (inlinedArg != arg);
            }
            if (rheChanged) {
                rhe.add(new FunctionCallExpression(c.name, args2));
            } else {
                rhe.add(c);
            }
            changed |= rheChanged;
        }

        var updated = changed ? new PipeExpression(lhe, rhe) : expr;

        var allLiteral = updated.chain.stream()
                .flatMap(f -> f.args.stream())
                .allMatch(LiteralExpression.class::isInstance)
                && updated.left instanceof LiteralExpression;

        if (allLiteral) {
            try {
                var result = evaluator.evaluate(updated, Context.empty());
                return new LiteralExpression(result);
            } catch (Exception ignored) {
                // ignore inlining exceptions
            }
        }

        return updated;
    }

    @Override
    public Expression visitTernary(TernaryExpression expr) {
        var c = expr.condition.accept(this);
        var t = expr.ifTrue.accept(this);
        var f = expr.ifFalse.accept(this);
        if (c == expr.condition && t == expr.ifTrue && f == expr.ifFalse) {
            return expr;
        }

        if (c instanceof LiteralExpression) {
            var cond = ((LiteralExpression) c).value;
            if (cond instanceof Boolean) {
                return (boolean) cond ? t : f;
            }
        }
        return new TernaryExpression(c, t, f);
    }
}

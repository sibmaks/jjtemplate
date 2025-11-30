package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.util.ArrayList;

/**
 * Interpreter for TemplateParser AST.
 * <p>
 * Pipe semantics: left expression result is passed as a *separate* last parameter (Object pipeInput)
 * to all function invocations, instead of being appended to the args list.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class TemplateEvaluator {
    private final FunctionRegistry functionRegistry;

    public TemplateEvaluator(TemplateEvaluationOptions evaluationOptions) {
        this.functionRegistry = new FunctionRegistry(evaluationOptions);
    }

    public Object evaluate(Expression expression, Context context) {
        try {
            if (expression instanceof LiteralExpression) {
                var literalExpression = (LiteralExpression) expression;
                return literalExpression.value;
            }
            if (expression instanceof VariableExpression) {
                var variableExpression = (VariableExpression) expression;
                return evalVariable(variableExpression, context);
            }
            if (expression instanceof FunctionCallExpression) {
                var callExpression = (FunctionCallExpression) expression;
                return evalCall(callExpression, context);
            }
            if (expression instanceof PipeExpression) {
                var pipeExpression = (PipeExpression) expression;
                return evalPipe(pipeExpression, context);
            }
            if (expression instanceof TernaryExpression) {
                var ternary = (TernaryExpression) expression;
                var cond = evaluate(ternary.condition, context);
                if (!(cond instanceof Boolean)) {
                    throw new TemplateEvalException("cond must be a boolean: " + cond);
                }
                var test = (boolean) cond;
                if (test) {
                    return evaluate(ternary.ifTrue, context);
                }
                return evaluate(ternary.ifFalse, context);
            }
            throw new TemplateEvalException("Unknown expr type: " + expression.getClass());
        } catch (Exception e) {
            var printVisitor = new PrettyPrintVisitor();
            throw new TemplateEvalException(
                    String.format(
                            "Exception on expression \"%s\" evaluation: %s",
                            expression.accept(printVisitor),
                            e.getMessage()
                    ),
                    e
            );
        }
    }

    private Object evalVariable(
            VariableExpression variableExpression,
            Context context
    ) {
        if (variableExpression.segments.isEmpty()) {
            return null;
        }

        var first = variableExpression.segments.get(0);
        var current = context.getRoot(first.name);

        for (var i = 1; i < variableExpression.segments.size(); i++) {
            var seg = variableExpression.segments.get(i);
            if (current == null) {
                return null;
            }

            if (!seg.isMethod()) {
                current = ReflectionUtils.getProperty(current, seg.name);
                continue;
            }

            var args = new ArrayList<>();
            for (var argExpr : seg.args) {
                args.add(evaluate(argExpr, context));
            }
            current = ReflectionUtils.invokeMethodReflective(current, seg.name, args);
        }

        return current;
    }

    private Object evalPipe(PipeExpression p, Context ctx) {
        var cur = evaluate(p.left, ctx);
        for (var call : p.chain) {
            cur = evalCall(call, ctx, cur);
        }
        return cur;
    }

    private Object evalCall(
            FunctionCallExpression c,
            Context context,
            Object pipeInput
    ) {
        var args = new ArrayList<>();
        for (var a : c.args) {
            args.add(evaluate(a, context));
        }
        var templateFunction = functionRegistry.getFunction(c.namespace, c.name);
        return templateFunction.invoke(args, pipeInput);
    }

    private Object evalCall(
            FunctionCallExpression c,
            Context context
    ) {
        var args = new ArrayList<>();
        for (var a : c.args) {
            args.add(evaluate(a, context));
        }
        var templateFunction = functionRegistry.getFunction(c.namespace, c.name);
        return templateFunction.invoke(args);
    }

}

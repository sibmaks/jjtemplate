package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.util.ArrayList;
import java.util.List;

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

    private static Class<?> wrap(Class<?> cls) {
        if (!cls.isPrimitive()) {
            return cls;
        }
        switch (cls.getName()) {
            case "int":
                return Integer.class;
            case "boolean":
                return Boolean.class;
            case "long":
                return Long.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "char":
                return Character.class;
            case "short":
                return Short.class;
            case "byte":
                return Byte.class;
            default:
                return cls;
        }
    }

    public Object evaluate(Expression expression, Context context) {
        return eval(expression, context);
    }

    private Object eval(
            Expression expression,
            Context context
    ) {
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
            var cond = eval(ternary.condition, context);
            if (!(cond instanceof Boolean)) {
                throw new TemplateEvalException("cond must be a boolean: " + cond);
            }
            var test = (boolean) cond;
            if (test) {
                return eval(ternary.ifTrue, context);
            } else {
                return eval(ternary.ifFalse, context);
            }
        }
        throw new TemplateEvalException("Unknown expr type: " + expression.getClass());
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
                args.add(eval(argExpr, context));
            }
            current = invokeMethodReflective(current, seg.name, args);
        }

        return current;
    }

    private Object invokeMethodReflective(Object target, String methodName, List<Object> args) {
        var type = target.getClass();
        var methods = type.getMethods();

        outer:
        for (var m : methods) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            var params = m.getParameterTypes();
            if (params.length != args.size()) {
                continue;
            }

            var converted = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                var arg = args.get(i);
                if (arg == null) {
                    converted[i] = null;
                    continue;
                }
                if (!wrap(params[i]).isAssignableFrom(arg.getClass())) {
                    continue outer;
                }
                converted[i] = arg;
            }

            try {
                return m.invoke(target, converted);
            } catch (Exception e) {
                throw new TemplateEvalException("Error calling method " + methodName, e);
            }
        }

        throw new IllegalArgumentException("No method " + methodName + " with args " + args.size());
    }

    private Object evalPipe(PipeExpression p, Context ctx) {
        var cur = eval(p.left, ctx);
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
            args.add(eval(a, context));
        }
        var templateFunction = functionRegistry.getFunction(c.name);
        return templateFunction.invoke(args, pipeInput);
    }

    private Object evalCall(
            FunctionCallExpression c,
            Context context
    ) {
        var args = new ArrayList<>();
        for (var a : c.args) {
            args.add(eval(a, context));
        }
        var templateFunction = functionRegistry.getFunction(c.name);
        return templateFunction.invoke(args);
    }

}

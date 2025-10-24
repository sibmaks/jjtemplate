package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.*;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic.*;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.math.NegTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.FormatStringTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.StringLowerTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.StringUpperTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.util.*;

/**
 * Interpreter for TemplateParser AST.
 * <p>
 * Pipe semantics: left expression result is passed as a *separate* last parameter (Object pipeInput)
 * to all function invocations, instead of being appended to the args list.
 */
public final class TemplateEvaluator {
    private final Map<String, TemplateFunction> functions;

    public TemplateEvaluator(Locale locale) {
        this(locale, Map.of());
    }

    public TemplateEvaluator(Locale locale, Map<String, TemplateFunction> functions) {
        var allFunctions = new HashMap<>(functions);
        var builtInFunctions = List.of(
                new BooleanTemplateFunction(),
                new FloatTemplateFunction(),
                new IntTemplateFunction(),
                new StrTemplateFunction(),
                new ConcatTemplateFunction(),
                new StringLowerTemplateFunction(locale),
                new StringUpperTemplateFunction(locale),
                new EmptyTemplateFunction(),
                new LengthTemplateFunction(),
                new ListTemplateFunction(),
                new EqualsTemplateFunction(),
                new NotEqualsTemplateFunction(),
                new NotTemplateFunction(),
                new DefaultTemplateFunction(),
                new LTCompareTemplateFunction(),
                new LECompareTemplateFunction(),
                new GTCompareTemplateFunction(),
                new GECompareTemplateFunction(),
                new AndTemplateFunction(),
                new OrTemplateFunction(),
                new FormatDateTemplateFunction(),
                new FormatStringTemplateFunction(locale),
                new NegTemplateFunction(),
                new CollapseTemplateFunction(),
                new ParseDateTemplateFunction(),
                new ParseDateTimeTemplateFunction()
        );
        for (var builtInFunction : builtInFunctions) {
            allFunctions.put(builtInFunction.getName(), builtInFunction);
        }
        this.functions = allFunctions;
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

    public ExpressionValue evaluate(Expression expression, Context context) {
        return eval(expression, context);
    }

    private ExpressionValue eval(
            Expression expression,
            Context context
    ) {
        if (expression instanceof LiteralExpression) {
            var literalExpression = (LiteralExpression) expression;
            return ExpressionValue.of(literalExpression.value);
        }
        if (expression instanceof VariableExpression) {
            var variableExpression = (VariableExpression) expression;
            return evalVariable(variableExpression, context);
        }
        if (expression instanceof FunctionCallExpression) {
            var callExpression = (FunctionCallExpression) expression;
            return evalCall(callExpression, context, ExpressionValue.empty());
        }
        if (expression instanceof PipeExpression) {
            var pipeExpression = (PipeExpression) expression;
            return evalPipe(pipeExpression, context);
        }
        if (expression instanceof TernaryExpression) {
            var ternary = (TernaryExpression) expression;
            var cond = eval(ternary.condition, context).getValue();
            if (!(cond instanceof Boolean)) {
                throw new IllegalArgumentException("cond must be a boolean: " + cond);
            }
            var test = (Boolean) cond;
            if (test) {
                return eval(ternary.ifTrue, context);
            } else {
                return eval(ternary.ifFalse, context);
            }
        }
        throw new TemplateEvalException("Unknown expr type: " + expression.getClass());
    }

    private ExpressionValue evalVariable(
            VariableExpression variableExpression,
            Context context
    ) {
        if (variableExpression.segments.isEmpty()) {
            return ExpressionValue.empty();
        }

        var first = variableExpression.segments.get(0);
        var curr = context.getRoot(first.name);
        if (curr.isEmpty()) {
            return ExpressionValue.empty();
        }
        var current = curr.getValue();

        for (var i = 1; i < variableExpression.segments.size(); i++) {
            var seg = variableExpression.segments.get(i);
            if (current == null) {
                return ExpressionValue.empty();
            }

            if (!seg.isMethod()) {
                current = ReflectionUtils.getProperty(current, seg.name);
                continue;
            }

            var args = new ArrayList<>();
            for (var argExpr : seg.args) {
                args.add(eval(argExpr, context).getValue());
            }
            current = invokeMethodReflective(current, seg.name, args);
        }

        return ExpressionValue.of(current);
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
                throw new RuntimeException("Error calling method " + methodName, e);
            }
        }

        throw new IllegalArgumentException("No method " + methodName + " with args " + args.size());
    }

    private ExpressionValue evalPipe(PipeExpression p, Context ctx) {
        var cur = eval(p.left, ctx);
        for (var call : p.chain) {
            cur = evalCall(call, ctx, cur);
        }
        return cur;
    }

    private ExpressionValue evalCall(
            FunctionCallExpression c,
            Context context,
            ExpressionValue pipeInput
    ) {
        var args = new ArrayList<ExpressionValue>();
        for (var a : c.args) {
            args.add(eval(a, context));
        }
        var templateFunction = functions.get(c.name);
        if (templateFunction == null) {
            throw new TemplateEvalException(String.format("Function '%s' not found", c.name));
        }
        return templateFunction.invoke(args, pipeInput);
    }

}

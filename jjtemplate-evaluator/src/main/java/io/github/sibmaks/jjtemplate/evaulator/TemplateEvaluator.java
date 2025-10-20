package io.github.sibmaks.jjtemplate.evaulator;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.*;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.string.StringLowerTemplateFunction;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.string.StringUpperTemplateFunction;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Interpreter for TemplateParser AST.
 * <p>
 * Pipe semantics: left expression result is passed as a *separate* last parameter (Object pipeInput)
 * to all function invocations, instead of being appended to the args list.
 */
public final class TemplateEvaluator {
    private static final List<TemplateFunction> builtInFunctions = List.of(
            new BooleanTemplateFunction(),
            new DoubleTemplateFunction(),
            new IntTemplateFunction(),
            new StrTemplateFunction(),
            new ConcatTemplateFunction(),
            new StringLowerTemplateFunction(),
            new StringUpperTemplateFunction(),
            new EmptyTemplateFunction(),
            new LengthTemplateFunction(),
            new ListTemplateFunction()
    );

    private final Map<String, TemplateFunction> functions;

    public TemplateEvaluator() {
        this(Map.of());
    }

    public TemplateEvaluator(Map<String, TemplateFunction> functions) {
        var allFunctions = new HashMap<>(functions);
        for (var builtInFunction : builtInFunctions) {
            allFunctions.put(builtInFunction.getName(), builtInFunction);
        }
        this.functions = allFunctions;
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
        throw new TemplateEvalException("Unknown expr type: " + expression.getClass());
    }

    private ExpressionValue evalVariable(
            VariableExpression variableExpression,
            Context context
    ) {
        if (variableExpression.path.isEmpty()) {
            return ExpressionValue.empty();
        }
        var cur = context.getRoot(variableExpression.path.get(0));
        for (int i = 1; i < variableExpression.path.size(); i++) {
            if (cur == null || cur.isEmpty()) {
                return ExpressionValue.empty();
            }
            var currValue = cur.getValue();
            var seg = variableExpression.path.get(i);
            if (currValue instanceof Map) {
                var map = (Map<?, ?>) currValue;
                cur = ExpressionValue.of(map.get(seg));
            } else if (currValue instanceof List && isInt(seg)) {
                var list = (List<?>) currValue;
                var idx = Integer.parseInt(seg);
                if (idx >= 0 && idx < list.size()) {
                    cur = ExpressionValue.of(list.get(idx));
                } else {
                    throw new IllegalArgumentException(String.format("Index '%d' out of list length: %s", idx, currValue));
                }
            } else if (currValue.getClass().isArray() && isInt(seg)) {
                var idx = Integer.parseInt(seg);
                var len = Array.getLength(currValue);
                if (idx >= 0 && idx < len) {
                    cur = ExpressionValue.of(Array.get(currValue, idx));
                } else {
                    throw new IllegalArgumentException(String.format("Index '%d' out of array length: %s", idx, len));
                }
            } else if (currValue instanceof CharSequence && isInt(seg)) {
                var idx = Integer.parseInt(seg);
                var curLine = (CharSequence) currValue;
                if (idx >= 0 && idx < curLine.length()) {
                    cur = ExpressionValue.of(Character.toString(curLine.charAt(idx)));
                } else {
                    throw new IllegalArgumentException(String.format("Index '%d' out of string length: %s", idx, curLine));
                }
            } else {
                throw new IllegalArgumentException(String.format("Unsupported type '%s' for dot operator for segment '%s", currValue.getClass(), seg));
            }
        }
        return cur;
    }

    private boolean isInt(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return !s.isEmpty();
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

    private Object invoke(String name, List<Object> args, Object pipeInput) {
        switch (name) {
            case "not":
                return fnNot(args, pipeInput);
            case "eq":
                return fnEq(args, pipeInput);
            case "neq":
                return fnNeq(args, pipeInput);
            case "lt":
                return fnCmp(args, pipeInput, -1, false);
            case "le":
                return fnCmp(args, pipeInput, -1, true);
            case "gt":
                return fnCmp(args, pipeInput, 1, false);
            case "ge":
                return fnCmp(args, pipeInput, 1, true);
            case "and":
                return fnAnd(args, pipeInput);
            case "or":
                return fnOr(args, pipeInput);
            case "optional":
                return fnOptional(args, pipeInput);
            case "default":
                return fnDefault(args, pipeInput);
            default:
                throw new TemplateEvalException("Unknown function: " + name);
        }
    }

    // === Implementations ===
    private Object first(List<Object> a, Object pipe) {
        return !a.isEmpty() ? a.get(0) : pipe;
    }

    private Object fnNot(List<Object> a, Object p) {
        var v = first(a, p);
        if (!(v instanceof Boolean)) {
            throw new TemplateEvalException("not: arg not boolean");
        }
        return !((Boolean) v);
    }

    private Object fnEq(List<Object> a, Object p) {
        if (a.size() == 1) {
            return Objects.equals(a.get(0), p);
        }
        if (a.size() == 2) {
            return Objects.equals(a.get(0), a.get(1));
        }
        throw new TemplateEvalException("eq: expected 1 or 2 args");
    }

    private Object fnNeq(List<Object> a, Object p) {
        var r = fnEq(a, p);
        return !(Boolean) r;
    }

    private Object fnCmp(List<Object> a, Object p, int dir, boolean eq) {
        Object x, y;
        if (a.size() == 1) {
            x = p;
            y = a.get(0);
        } else if (a.size() == 2) {
            x = a.get(0);
            y = a.get(1);
        } else {
            throw new TemplateEvalException("cmp: invalid args");
        }
        var nx = asNum(x);
        var ny = asNum(y);
        var c = Double.compare(nx.doubleValue(), ny.doubleValue());
        return dir < 0 ? (eq ? c <= 0 : c < 0) : (eq ? c >= 0 : c > 0);
    }

    private Object fnAnd(List<Object> a, Object p) {
        var x = (Boolean) first(a, p);
        var y = (Boolean) (a.size() > 1 ? a.get(1) : p);
        return x && y;
    }

    private Object fnOr(List<Object> a, Object p) {
        var x = (Boolean) first(a, p);
        var y = (Boolean) (a.size() > 1 ? a.get(1) : p);
        return x || y;
    }

    private Object fnOptional(List<Object> a, Object p) {
        if (a.size() == 1) {
            return ((Boolean) a.get(0)) ? p : Omit.INSTANCE;
        }
        if (a.size() == 2) {
            return ((Boolean) a.get(0)) ? a.get(1) : Omit.INSTANCE;
        }
        throw new TemplateEvalException("optional: invalid args");
    }

    private Object fnDefault(List<Object> a, Object p) {
        if (a.isEmpty()) {
            return p;
        }
        if (a.size() == 1) {
            return p != null ? p : a.get(0);
        }
        if (a.size() == 2) {
            return a.get(0) != null ? a.get(0) : a.get(1);
        }
        throw new TemplateEvalException("default: invalid args");
    }

    private Number asNum(Object v) {
        if (v instanceof Number) {
            return (Number) v;
        }
        if (v instanceof String) {
            try {
                return Double.parseDouble((String) v);
            } catch (Exception ignored) {
            }
        }
        throw new TemplateEvalException("Expected number: " + v);
    }

}

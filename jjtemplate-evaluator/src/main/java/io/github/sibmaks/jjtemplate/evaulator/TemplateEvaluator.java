package io.github.sibmaks.jjtemplate.evaulator;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.*;
import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.Token;
import io.github.sibmaks.jjtemplate.lexer.TokenType;
import io.github.sibmaks.jjtemplate.parser.TemplateParser;
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
            new ConcatTemplateFunction()
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

    public ExpressionValue evaluate(Expression expr, Context ctx) {
        return eval(expr, ctx);
    }

    private ExpressionValue eval(Expression e, Context ctx) {
        if (e instanceof LiteralExpression) {
            var literalExpression = (LiteralExpression) e;
            return ExpressionValue.of(literalExpression.value);
        }
        if (e instanceof VariableExpression) {
            var variableExpression = (VariableExpression) e;
            return evalVariable(variableExpression, ctx);
        }
        if (e instanceof FunctionCallExpression) {
            var callExpression = (FunctionCallExpression) e;
            return evalCall(callExpression, ctx, null);
        }
        if (e instanceof PipeExpression) {
            var pipeExpression = (PipeExpression) e;
            return evalPipe(pipeExpression, ctx);
        }
        throw new TemplateEvalException("Unknown expr type: " + e.getClass());
    }

    private ExpressionValue evalVariable(VariableExpression v, Context ctx) {
        if (v.path.isEmpty()) {
            return ExpressionValue.empty();
        }
        var cur = ctx.getRoot(v.path.get(0));
        for (int i = 1; i < v.path.size(); i++) {
            if (cur == null) {
                return ExpressionValue.empty();
            }
            var seg = v.path.get(i);
            if (cur instanceof Map) {
                var map = (Map<?, ?>) cur;
                cur = ExpressionValue.of(map.get(seg));
            } else if (cur instanceof List && isInt(seg)) {
                var list = (List<?>) cur;
                var idx = Integer.parseInt(seg);
                if (idx >= 0 && idx < list.size()) {
                    cur = ExpressionValue.of(list.get(idx));
                } else {
                    cur = ExpressionValue.empty();
                }
            } else if (cur.getClass().isArray() && isInt(seg)) {
                var idx = Integer.parseInt(seg);
                var len = Array.getLength(cur);
                if (idx >= 0 && idx < len) {
                    cur = ExpressionValue.of(Array.get(cur.getValue(), idx));
                } else {
                    cur = ExpressionValue.empty();
                }
            } else {
                return ExpressionValue.empty();
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
            Context ctx,
            ExpressionValue pipeInput
    ) {
        var args = new ArrayList<ExpressionValue>();
        for (var a : c.args) {
            args.add(eval(a, ctx));
        }
        var templateFunction = functions.get(c.name);
        if(templateFunction == null) {
            throw new TemplateEvalException(String.format("Function '%s' not found", c.name));
        }
        return templateFunction.invoke(args, pipeInput);
    }

    private Object invoke(String name, List<Object> args, Object pipeInput) {
        switch (name) {
            case "len":
                return fnLen(args, pipeInput);
            case "empty":
                return fnEmpty(args, pipeInput);
            case "upper":
                return fnUpper(args, pipeInput);
            case "lower":
                return fnLower(args, pipeInput);
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
            case "list":
                return fnList(args, pipeInput);
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

    private Object fnLen(List<Object> a, Object p) {
        var v = first(a, p);
        if (v == null) {
            return 0;
        }
        if (v instanceof CharSequence) {
            return ((CharSequence) v).length();
        }
        if (v instanceof Collection) {
            return ((Collection<?>) v).size();
        }
        if (v instanceof Map) {
            return ((Map<?, ?>) v).size();
        }
        if (v.getClass().isArray()) {
            return Array.getLength(v);
        }
        throw new TemplateEvalException("len: unsupported type: " + v.getClass());
    }

    private Object fnEmpty(List<Object> a, Object p) {
        var v = first(a, p);
        if (v == null) {
            return true;
        }
        if (v instanceof CharSequence) {
            return ((CharSequence) v).length() == 0;
        }
        if (v instanceof Collection) {
            return ((Collection<?>) v).isEmpty();
        }
        if (v instanceof Map) {
            return ((Map<?, ?>) v).isEmpty();
        }
        if (v.getClass().isArray()) {
            return Array.getLength(v) == 0;
        }
        throw new TemplateEvalException("empty: unsupported type: " + v.getClass());
    }

    private Object fnUpper(List<Object> a, Object p) {
        var v = first(a, p);
        return v == null ? null : String.valueOf(v).toUpperCase(Locale.ROOT);
    }

    private Object fnLower(List<Object> a, Object p) {
        var v = first(a, p);
        return v == null ? null : String.valueOf(v).toLowerCase(Locale.ROOT);
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

    private Object fnList(List<Object> a, Object p) {
        return new ArrayList<>(a.isEmpty() ? List.of(p) : a);
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


    public static void main(String[] args) {
        String tpl = "{{ .var1 | concat 'tru' | boolean }}";
        TemplateLexer lx = new TemplateLexer(tpl);
        List<Token> toks = lx.tokens();
        toks.removeIf(t -> t.type == TokenType.TEXT || t.type == TokenType.OPEN_EXPR || t.type == TokenType.CLOSE);
        var parser = new TemplateParser(toks);
        Expression expr = parser.parseExpression();

        TemplateEvaluator ev = new TemplateEvaluator();
        Map<String, Object> ctx = Map.of("var1", "e");
        System.out.println(ev.evaluate(expr, new Context(ctx))); // true
    }
}

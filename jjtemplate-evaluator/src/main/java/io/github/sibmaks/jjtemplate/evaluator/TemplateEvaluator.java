package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.*;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic.*;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.math.NegTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.FormatStringTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.StringLowerTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.StringUpperTemplateFunction;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            new FormatStringTemplateFunction(),
            new NegTemplateFunction()
    );
    private static final Map<Class<?>, Map<String, java.lang.reflect.Method>> METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new HashMap<>();
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

    private static Map<String, Method> scanMethods(Class<?> cls) {
        var map = new HashMap<String, Method>();
        for (var m : cls.getMethods()) {
            if (m.getParameterCount() != 0) {
                continue;
            }
            var name = m.getName();
            if (name.startsWith("get") && name.length() > 3) {
                map.put(decapitalize(name.substring(3)), m);
            } else if (name.startsWith("is") && name.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                map.put(decapitalize(name.substring(2)), m);
            }
        }
        return map;
    }

    private static Map<String, Field> scanFields(Class<?> cls) {
        var map = new HashMap<String, Field>();
        for (var f : cls.getFields()) {
            f.setAccessible(true);
            map.put(f.getName(), f);
        }
        return map;
    }

    private static String decapitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
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
            if (cur.isEmpty()) {
                return ExpressionValue.empty();
            }
            var currValue = cur.getValue();
            var seg = variableExpression.path.get(i);
            if (currValue instanceof Map<?, ?>) {
                var map = (Map<?, ?>) currValue;
                cur = ExpressionValue.of(map.get(seg));
                continue;
            }
            if (currValue instanceof List<?> && isInt(seg)) {
                var list = (List<?>) currValue;
                var idx = Integer.parseInt(seg);
                if (idx < 0 || idx >= list.size()) {
                    throw new IllegalArgumentException("List index out of range: " + seg);
                }
                cur = ExpressionValue.of(list.get(idx));
                continue;
            }
            if (currValue.getClass().isArray() && isInt(seg)) {
                var idx = Integer.parseInt(seg);
                var len = Array.getLength(currValue);
                if (idx < 0 || idx >= len) {
                    throw new IllegalArgumentException("Array index out of range: " + seg);
                }
                cur = ExpressionValue.of(Array.get(currValue, idx));
                continue;
            }
            if (currValue instanceof CharSequence && isInt(seg)) {
                var idx = Integer.parseInt(seg);
                var seq = (CharSequence) currValue;
                if (idx < 0 || idx >= seq.length()) {
                    throw new IllegalArgumentException("String index out of range: " + seg);
                }
                cur = ExpressionValue.of(Character.toString(seq.charAt(idx)));
                continue;
            }
            cur = ExpressionValue.of(resolvePropertyReflective(currValue, seg));
        }
        return cur;
    }

    private Object resolvePropertyReflective(Object obj, String name) {
        var cls = obj.getClass();

        // --- Field lookup cache ---
        var fieldMap = FIELD_CACHE.computeIfAbsent(cls, TemplateEvaluator::scanFields);
        if (fieldMap.containsKey(name)) {
            try {
                var f = fieldMap.get(name);
                return f.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field '" + name + "' of " + cls, e);
            }
        }

        // --- Method lookup cache ---
        var methodMap = METHOD_CACHE.computeIfAbsent(cls, TemplateEvaluator::scanMethods);
        var m = methodMap.get(name);
        if (m != null) {
            try {
                return m.invoke(obj);
            } catch (Exception e) {
                throw new RuntimeException("Error invoking getter '" + name + "' on " + cls, e);
            }
        }

        throw new IllegalArgumentException("Unknown property '" + name + "' for class " + cls.getName());
    }

    private boolean isInt(String s) {
        for (var i = 0; i < s.length(); i++) {
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

}

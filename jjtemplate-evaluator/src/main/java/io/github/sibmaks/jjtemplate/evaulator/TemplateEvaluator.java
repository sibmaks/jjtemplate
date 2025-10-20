package io.github.sibmaks.jjtemplate.evaulator;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaulator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.*;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.DefaultTemplateFunction;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.logic.*;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.string.StringLowerTemplateFunction;
import io.github.sibmaks.jjtemplate.evaulator.fun.impl.string.StringUpperTemplateFunction;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.lang.reflect.Array;
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
            new OptionalTemplateFunction(),
            new LTCompareTemplateFunction(),
            new LECompareTemplateFunction(),
            new GTCompareTemplateFunction(),
            new GECompareTemplateFunction(),
            new AndTemplateFunction(),
            new OrTemplateFunction()
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

}

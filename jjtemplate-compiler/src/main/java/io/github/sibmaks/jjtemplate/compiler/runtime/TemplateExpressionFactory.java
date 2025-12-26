package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchDefinitionTemplateExpression;
import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses raw template expression text into {@link TemplateExpression}
 * instances used by the evaluator.
 * <p>
 * Wraps the parser expressions and provides a higher-level API for
 * constructing expression trees supporting operators, function calls,
 * property access, ternary expressions and pipelines.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateExpressionFactory implements ExpressionVisitor<TemplateExpression> {
    private final FunctionRegistry functionRegistry;

    /**
     * Creates a factory configured with evaluation options.
     *
     * @param evaluationOptions evaluation configuration
     */
    public TemplateExpressionFactory(TemplateEvaluationOptions evaluationOptions) {
        this.functionRegistry = new FunctionRegistry(evaluationOptions);
    }

    /**
     * Compiles a parsed template context into a template expression.
     *
     * @param context parsed template context
     * @return compiled template expression
     */
    public TemplateExpression compile(JJTemplateParser.TemplateContext context) {
        var parts = context.getParts();
        if (parts.isEmpty()) {
            return new ConstantTemplateExpression("");
        }

        var expressions = new ArrayList<TemplateExpression>(parts.size());
        for (var part : parts) {
            if (part instanceof JJTemplateParser.TextPart) {
                var textPart = (JJTemplateParser.TextPart) part;
                expressions.add(new ConstantTemplateExpression(textPart.getText()));
            } else if (part instanceof JJTemplateParser.InterpolationPart) {
                var interpolation = (JJTemplateParser.InterpolationPart) part;
                expressions.add(compile(interpolation.getExpression()));
            }
        }

        if (expressions.size() == 1) {
            return expressions.get(0);
        }

        return new TemplateConcatTemplateExpression(expressions);
    }

    /**
     * Compiles a parsed expression into a template expression.
     *
     * @param expression parsed expression
     * @return compiled template expression
     */
    public TemplateExpression compile(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public TemplateExpression visitLiteral(LiteralExpression expr) {
        return new ConstantTemplateExpression(expr.value);
    }

    @Override
    public TemplateExpression visitVariable(VariableExpression expr) {
        var segments = expr.segments;
        if (segments.isEmpty()) {
            return new VariableTemplateExpression("", List.of());
        }
        var rootName = segments.get(0).name;
        var callChain = new ArrayList<VariableTemplateExpression.Chain>(segments.size());

        for (int i = 1; i < segments.size(); i++) {
            var segment = segments.get(i);
            if (!segment.isMethod()) {
                callChain.add(new VariableTemplateExpression.GetPropertyChain(segment.name));
                continue;
            }
            var argExpressions = new ArrayList<TemplateExpression>(segment.args.size());
            for (var argExpr : segment.args) {
                argExpressions.add(compile(argExpr));
            }
            callChain.add(new VariableTemplateExpression.CallMethodChain(segment.name, argExpressions));
        }

        return new VariableTemplateExpression(rootName, callChain);
    }

    @Override
    public TemplateExpression visitFunction(FunctionCallExpression expr) {
        var function = functionRegistry.getFunction(expr.namespace, expr.name);
        var argExpressions = new ArrayList<TemplateExpression>(expr.args.size());
        for (var argExpr : expr.args) {
            argExpressions.add(compile(argExpr));
        }
        return new FunctionCallTemplateExpression(function, argExpressions);
    }

    @Override
    public TemplateExpression visitPipe(PipeExpression expr) {
        var pipes = new ArrayList<FunctionCallTemplateExpression>(expr.chain.size());
        for (var functionCall : expr.chain) {
            var function = functionRegistry.getFunction(functionCall.namespace, functionCall.name);
            var argExpressions = new ArrayList<TemplateExpression>(functionCall.args.size());
            for (var argExpr : functionCall.args) {
                argExpressions.add(compile(argExpr));
            }
            pipes.add(new FunctionCallTemplateExpression(function, argExpressions));
        }

        var previous = compile(expr.left);
        return new PipeChainTemplateExpression(previous, pipes);
    }

    @Override
    public TemplateExpression visitTernary(TernaryExpression expr) {
        return new TernaryTemplateExpression(
                compile(expr.condition),
                compile(expr.ifTrue),
                compile(expr.ifFalse)
        );
    }

    @Override
    public TemplateExpression visitSwitch(SwitchExpression expr) {
        var keyExpression = compile(expr.key);
        var condition = compile(expr.condition);
        return SwitchDefinitionTemplateExpression.builder()
                .key(keyExpression)
                .condition(condition)
                .build();
    }

    @Override
    public TemplateExpression visitRange(RangeExpression expr) {
        return RangeTemplateExpression.builder()
                .name(compile(expr.name))
                .itemVariableName(expr.itemVariableName)
                .indexVariableName(expr.indexVariableName)
                .source(compile(expr.source))
                .build();
    }

    @Override
    public TemplateExpression visitThenSwitchCase(ThenSwitchCaseExpression expr) {
        if (expr.condition == null) {
            return new ConstantTemplateExpression(true);
        }
        var condition = compile(expr.condition);
        return SwitchDefinitionTemplateExpression.builder()
                .key(new ConstantTemplateExpression(true))
                .condition(condition)
                .build();
    }

    @Override
    public TemplateExpression visitElseSwitchCase(ElseSwitchCaseExpression expr) {
        if (expr.condition == null) {
            return new ConstantTemplateExpression(false);
        }
        var condition = compile(expr.condition);
        return SwitchDefinitionTemplateExpression.builder()
                .key(new ConstantTemplateExpression(false))
                .condition(condition)
                .build();
    }
}

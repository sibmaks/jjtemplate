package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchDefinitionTemplateExpression;
import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParser;
import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParserBaseVisitor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * Parses raw template expression text into {@link TemplateExpression}
 * instances used by the evaluator.
 * <p>
 * Wraps the ANTLR-generated parser and provides a higher-level API for
 * constructing expression trees supporting operators, function calls,
 * property access, ternary expressions and pipelines.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateExpressionFactory extends JJTemplateParserBaseVisitor<TemplateExpression> {
    private final FunctionRegistry functionRegistry;

    /**
     * Creates a factory configured with evaluation options.
     *
     * @param evaluationOptions evaluation configuration
     */
    public TemplateExpressionFactory(TemplateEvaluationOptions evaluationOptions) {
        this.functionRegistry = new FunctionRegistry(evaluationOptions);
    }

    @Override
    public TemplateExpression visitTemplate(JJTemplateParser.TemplateContext context) {
        var childCount = context.getChildCount();

        if (childCount == 1) {
            return new ConstantTemplateExpression("");
        }

        var expressions = new ArrayList<TemplateExpression>(childCount);

        for (int i = 0; i < childCount; i++) {
            var child = context.getChild(i);
            var expression = child.accept(this);
            if (expression != null) {
                expressions.add(expression);
            }
        }

        if (expressions.size() == 1) {
            return expressions.get(0);
        }

        return new TemplateConcatTemplateExpression(expressions);
    }

    @Override
    public TemplateExpression visitText(JJTemplateParser.TextContext context) {
        return new ConstantTemplateExpression(context.getText());
    }

    @Override
    public TemplateExpression visitExprInterpolation(JJTemplateParser.ExprInterpolationContext context) {
        var child = context.getChild(1);
        return child.accept(this);
    }

    @Override
    public TemplateExpression visitCondInterpolation(JJTemplateParser.CondInterpolationContext context) {
        var child = context.getChild(1);
        return child.accept(this);
    }

    @Override
    public TemplateExpression visitSpreadInterpolation(JJTemplateParser.SpreadInterpolationContext context) {
        var child = context.getChild(1);
        return child.accept(this);
    }

    @Override
    public TemplateExpression visitSwitchExpression(JJTemplateParser.SwitchExpressionContext context) {
        var switchExpression = context.expression();
        var condition = switchExpression.accept(this);

        var switchTemplateExpressionBuilder = SwitchDefinitionTemplateExpression.builder()
                .condition(condition);

        var name = context.name;
        if (name != null) {
            switchTemplateExpressionBuilder
                    .key(new ConstantTemplateExpression(name.getText()));
        } else {
            var switchCase = context.switchCase;
            var switchKey = switchCase.accept(this);
            switchTemplateExpressionBuilder
                    .key(switchKey);
        }

        return switchTemplateExpressionBuilder.build();
    }

    @Override
    public TemplateExpression visitRangeExpression(JJTemplateParser.RangeExpressionContext context) {
        var name = getRangeName(context);
        var rangeSourceExpression = context.expression();
        var rangeSource = rangeSourceExpression.accept(this);

        return RangeTemplateExpression.builder()
                .name(name)
                .itemVariableName(context.item.getText())
                .indexVariableName(context.index.getText())
                .source(rangeSource)
                .build();
    }

    private TemplateExpression getRangeName(JJTemplateParser.RangeExpressionContext context) {
        if (context.name != null) {
            return new ConstantTemplateExpression(context.name.getText());
        }
        var rangeName = context.rangeName;
        return rangeName.accept(this);
    }

    @Override
    public TemplateExpression visitVariable(JJTemplateParser.VariableContext context) {
        var segments = context.segment();
        var rootName = segments.get(0).getText();

        var callChain = new ArrayList<VariableTemplateExpression.Chain>(segments.size());

        for (int i = 1; i < segments.size(); i++) {
            var segment = segments.get(i);
            if (segment.LPAREN() == null) {
                var propertyName = segment.name.getText();
                callChain.add(new VariableTemplateExpression.GetPropertyChain(propertyName));
            } else {
                var argListContext = segment.argList();
                var argExpressions = argListContext == null ? List.<TemplateExpression>of() : new ArrayList<TemplateExpression>(argListContext.primary().size());
                if (argListContext != null) {
                    for (var argExpr : argListContext.primary()) {
                        var expression = argExpr.accept(this);
                        argExpressions.add(expression);
                    }
                }
                var methodName = segment.name.getText();
                callChain.add(new VariableTemplateExpression.CallMethodChain(methodName, argExpressions));
            }
        }

        return new VariableTemplateExpression(rootName, callChain);
    }

    @Override
    public TemplateExpression visitTernarExpression(JJTemplateParser.TernarExpressionContext context) {
        var cause = context.ternarExpressionCondition();
        var conditionExpression = cause.accept(this);

        var thenTrue = context.ternarExpressionOnTrue();
        var thenTrueExpression = thenTrue.accept(this);

        var thenFalse = context.ternarExpressionOnFalse();
        var thenFalseExpression = thenFalse.accept(this);

        return new TernaryTemplateExpression(conditionExpression, thenTrueExpression, thenFalseExpression);
    }

    @Override
    public TemplateExpression visitParenExpression(JJTemplateParser.ParenExpressionContext context) {
        var expression = context.expression();
        return expression.accept(this);
    }

    @Override
    public TemplateExpression visitTerminal(TerminalNode node) {
        switch (node.getSymbol().getType()) {
            case JJTemplateParser.NULL:
                return new ConstantTemplateExpression(null);
            case JJTemplateParser.STRING: {
                var text = node.getText();
                var content = text.substring(1, text.length() - 1);
                return new ConstantTemplateExpression(content);
            }
            case JJTemplateParser.NUMBER: {
                var text = node.getText();
                var content = new BigInteger(text);
                return new ConstantTemplateExpression(content);
            }
            case JJTemplateParser.FLOAT_NUMBER: {
                var text = node.getText();
                var content = new BigDecimal(text);
                return new ConstantTemplateExpression(content);
            }
            case JJTemplateParser.BOOLEAN: {
                var isTrue = "true".equals(node.getText());
                return new ConstantTemplateExpression(isTrue);
            }
        }
        return null;
    }

    @Override
    public TemplateExpression visitFunctionCall(JJTemplateParser.FunctionCallContext context) {
        var namespace = Optional.ofNullable(context.namespace)
                .map(Token::getText)
                .orElse("");
        var name = context.functionName.getText();

        var function = functionRegistry.getFunction(namespace, name);

        var argExpressions = getArgExpressions(context);

        return new FunctionCallTemplateExpression(function, argExpressions);
    }

    @Override
    public TemplateExpression visitPipeExpr(JJTemplateParser.PipeExprContext context) {
        var pipes = new ArrayList<FunctionCallTemplateExpression>();
        for (var functionCallContext : context.functionCall()) {
            var namespace = Optional.ofNullable(functionCallContext.namespace)
                    .map(Token::getText)
                    .orElse("");
            var name = functionCallContext.functionName.getText();

            var function = functionRegistry.getFunction(namespace, name);

            var argExpressions = getArgExpressions(functionCallContext);

            pipes.add(new FunctionCallTemplateExpression(function, argExpressions));
        }

        var previous = context.primary().accept(this);
        return new PipeChainTemplateExpression(previous, pipes);
    }

    private List<TemplateExpression> getArgExpressions(JJTemplateParser.FunctionCallContext context) {
        var argListContext = context.argList();
        if (argListContext == null) {
            return List.of();
        }
        var argExpressions = new ArrayList<TemplateExpression>();
        for (var argExpr : argListContext.primary()) {
            var expression = argExpr.accept(this);
            argExpressions.add(expression);
        }
        return argExpressions;
    }

    @Override
    public TemplateExpression visitThenSwitchCaseExpression(JJTemplateParser.ThenSwitchCaseExpressionContext context) {
        var switchExpression = context.expression();

        if (switchExpression == null) {
            return new ConstantTemplateExpression(true);
        }

        var condition = switchExpression.accept(this);

        return SwitchDefinitionTemplateExpression.builder()
                .key(new ConstantTemplateExpression(true))
                .condition(condition)
                .build();
    }

    @Override
    public TemplateExpression visitElseSwitchCaseExpression(JJTemplateParser.ElseSwitchCaseExpressionContext context) {
        var switchExpression = context.expression();

        var condition = switchExpression.accept(this);

        return SwitchDefinitionTemplateExpression.builder()
                .key(new ConstantTemplateExpression(false))
                .condition(condition)
                .build();
    }
}

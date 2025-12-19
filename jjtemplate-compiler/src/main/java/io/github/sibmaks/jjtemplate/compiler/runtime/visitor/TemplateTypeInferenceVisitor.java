package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParser;
import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParserBaseVisitor;

import java.util.ArrayList;

/**
 * Performs static type inference for template parse tree nodes produced by ANTLR.
 * <p>
 * This visitor inspects interpolation constructs, text segments and composite
 * template fragments to determine the resulting {@link TemplateType}. The inferred
 * type helps the compiler decide how an expression should be evaluated or optimized.
 * </p>
 *
 * <p>Type inference rules:</p>
 * <ul>
 *   <li>Plain text → {@link TemplateType#CONSTANT}</li>
 *   <li>{{ expr }} → {@link TemplateType#EXPRESSION}</li>
 *   <li>{{? expr }} → {@link TemplateType#CONDITION}</li>
 *   <li>{{... expr }} → {@link TemplateType#SPREAD}</li>
 *   <li>Composite templates produce {@link TemplateType#EXPRESSION} unless they
 *       contain exactly one expression type fragment.</li>
 * </ul>
 *
 * <p>This visitor never throws and always returns a non-null type for valid nodes.</p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateTypeInferenceVisitor extends JJTemplateParserBaseVisitor<TemplateType> {

    @Override
    public TemplateType visitTemplate(JJTemplateParser.TemplateContext context) {
        var childCount = context.getChildCount();

        var expressions = new ArrayList<TemplateType>(childCount);

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

        return TemplateType.EXPRESSION;
    }

    @Override
    public TemplateType visitText(JJTemplateParser.TextContext context) {
        return TemplateType.CONSTANT;
    }

    @Override
    public TemplateType visitExprInterpolation(JJTemplateParser.ExprInterpolationContext context) {
        return context.getChild(1).accept(this);
    }

    @Override
    public TemplateType visitCondInterpolation(JJTemplateParser.CondInterpolationContext context) {
        return TemplateType.CONDITION;
    }

    @Override
    public TemplateType visitSpreadInterpolation(JJTemplateParser.SpreadInterpolationContext context) {
        return TemplateType.SPREAD;
    }

    @Override
    public TemplateType visitSwitchExpression(JJTemplateParser.SwitchExpressionContext context) {
        return TemplateType.SWITCH;
    }

    @Override
    public TemplateType visitThenSwitchCaseExpression(JJTemplateParser.ThenSwitchCaseExpressionContext context) {
        var expression = context.expression();
        if (expression != null) {
            return TemplateType.SWITCH;
        }
        return TemplateType.EXPRESSION;
    }

    @Override
    public TemplateType visitElseSwitchCaseExpression(JJTemplateParser.ElseSwitchCaseExpressionContext context) {
        var expression = context.expression();
        if (expression != null) {
            return TemplateType.SWITCH;
        }
        return TemplateType.SWITCH_ELSE;
    }

    @Override
    public TemplateType visitRangeExpression(JJTemplateParser.RangeExpressionContext context) {
        return TemplateType.RANGE;
    }
}

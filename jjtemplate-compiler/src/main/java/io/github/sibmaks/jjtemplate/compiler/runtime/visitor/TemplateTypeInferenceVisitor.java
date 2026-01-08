package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;
import io.github.sibmaks.jjtemplate.parser.api.ElseSwitchCaseExpression;
import io.github.sibmaks.jjtemplate.parser.api.RangeExpression;
import io.github.sibmaks.jjtemplate.parser.api.SwitchExpression;
import io.github.sibmaks.jjtemplate.parser.api.ThenSwitchCaseExpression;

/**
 * Performs static type inference for template parse contexts.
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
public final class TemplateTypeInferenceVisitor {

    /**
     * Infers the template type for a parsed template context.
     *
     * @param context template context
     * @return inferred template type
     */
    public TemplateType infer(JJTemplateParser.TemplateContext context) {
        var parts = context.getParts();
        if (parts.size() != 1) {
            return TemplateType.EXPRESSION;
        }
        var part = parts.get(0);
        if (part instanceof JJTemplateParser.TextPart) {
            return TemplateType.CONSTANT;
        }
        if (part instanceof JJTemplateParser.InterpolationPart) {
            var interpolation = (JJTemplateParser.InterpolationPart) part;
            switch (interpolation.getType()) {
                case CONDITION:
                    return TemplateType.CONDITION;
                case SPREAD:
                    return TemplateType.SPREAD;
                case EXPRESSION:
                default:
                    return inferExpression(interpolation.getExpression());
            }
        }
        return TemplateType.EXPRESSION;
    }

    private TemplateType inferExpression(Object expression) {
        if (expression instanceof SwitchExpression) {
            return TemplateType.SWITCH;
        }
        if (expression instanceof RangeExpression) {
            return TemplateType.RANGE;
        }
        if (expression instanceof ThenSwitchCaseExpression) {
            var thenExpression = (ThenSwitchCaseExpression) expression;
            return thenExpression.condition == null ? TemplateType.EXPRESSION : TemplateType.SWITCH;
        }
        if (expression instanceof ElseSwitchCaseExpression) {
            var elseExpression = (ElseSwitchCaseExpression) expression;
            return elseExpression.condition == null ? TemplateType.SWITCH_ELSE : TemplateType.SWITCH;
        }
        return TemplateType.EXPRESSION;
    }
}

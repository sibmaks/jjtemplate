package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.parser.ExpressionParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author sibmaks
 */
class TemplateTypeInferenceVisitorTest {
    private final ExpressionParser expressionParser = new ExpressionParser();
    private final TemplateTypeInferenceVisitor visitor = new TemplateTypeInferenceVisitor();

    @Test
    void inferConstantText() {
        var context = expressionParser.parse("static");
        assertEquals(TemplateType.CONSTANT, visitor.infer(context));
    }

    @Test
    void inferExpression() {
        var context = expressionParser.parse("{{ .value }}");
        assertEquals(TemplateType.EXPRESSION, visitor.infer(context));
    }

    @Test
    void inferCondition() {
        var context = expressionParser.parse("{{? .value }}");
        assertEquals(TemplateType.CONDITION, visitor.infer(context));
    }

    @Test
    void inferSpread() {
        var context = expressionParser.parse("{{. .value }}");
        assertEquals(TemplateType.SPREAD, visitor.infer(context));
    }

    @Test
    void inferSwitch() {
        var context = expressionParser.parse("{{ condition switch .value }}");
        assertEquals(TemplateType.SWITCH, visitor.infer(context));
    }

    @Test
    void inferRange() {
        var context = expressionParser.parse("{{ items range item, index of .values }}");
        assertEquals(TemplateType.RANGE, visitor.infer(context));
    }

    @Test
    void inferElseSwitchCase() {
        var context = expressionParser.parse("{{ else }}");
        assertEquals(TemplateType.SWITCH_ELSE, visitor.infer(context));
    }
}

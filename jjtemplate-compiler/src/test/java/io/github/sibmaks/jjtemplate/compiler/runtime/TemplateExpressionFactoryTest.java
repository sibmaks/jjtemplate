package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateConcatTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.VariableTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchDefinitionTemplateExpression;
import io.github.sibmaks.jjtemplate.parser.api.*;
import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
class TemplateExpressionFactoryTest {

    @Test
    void compileTemplateContextShouldSkipUnknownTemplatePartTypes() {
        var factory = new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build());
        var context = new JJTemplateParser.TemplateContext(List.of(
                new JJTemplateParser.TextPart("a"),
                new JJTemplateParser.TemplatePart() {
                }
        ));

        var expression = factory.compile(context);

        assertEquals("a", assertInstanceOf(ConstantTemplateExpression.class, expression).getValue());
    }

    @Test
    void visitVariableShouldSupportEmptySegments() {
        var factory = new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build());

        var expression = factory.visitVariable(new VariableExpression(List.of()));

        var variable = assertInstanceOf(VariableTemplateExpression.class, expression);
        assertEquals("", variable.getRootName());
        assertTrue(variable.getCallChain().isEmpty());
    }

    @Test
    void visitThenAndElseSwitchCasesWithoutConditionShouldReturnConstants() {
        var factory = new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build());

        var thenExpression = factory.visitThenSwitchCase(new ThenSwitchCaseExpression(null));
        var elseExpression = factory.visitElseSwitchCase(new ElseSwitchCaseExpression(null));

        assertEquals(true, assertInstanceOf(ConstantTemplateExpression.class, thenExpression).getValue());
        assertEquals(false, assertInstanceOf(ConstantTemplateExpression.class, elseExpression).getValue());
    }

    @Test
    void visitLiteralShouldRejectNonExpressionInterpolationInsideStringLiteral() {
        var factory = new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build());

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.visitLiteral(new LiteralExpression("x {{? true }}"))
        );

        assertEquals("Only '{{ ... }}' substitutions are allowed inside string literals", exception.getMessage());
    }

    @Test
    void visitElseSwitchCaseWithConditionShouldBuildSwitchDefinition() {
        var factory = new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build());

        var expression = factory.visitElseSwitchCase(new ElseSwitchCaseExpression(new LiteralExpression("fallback")));

        var switchDefinition = assertInstanceOf(SwitchDefinitionTemplateExpression.class, expression);
        assertEquals(false, assertInstanceOf(ConstantTemplateExpression.class, switchDefinition.getKey()).getValue());
        assertEquals("fallback", assertInstanceOf(ConstantTemplateExpression.class, switchDefinition.getCondition()).getValue());
    }

    @Test
    void visitLiteralWithEmbeddedExpressionShouldBuildConcat() {
        var factory = new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build());

        var expression = factory.visitLiteral(new LiteralExpression("Hi {{ .name }}"));

        assertInstanceOf(TemplateConcatTemplateExpression.class, expression);
    }
}

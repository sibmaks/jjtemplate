package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 *
 * @author sibmaks
 */
class TemplateConcatTemplateExpressionTest {

    @Test
    void applyShouldConcatenateEvaluatedChildExpressions() {
        Context context = mock();

        TemplateExpression expr1 = mock("firstExpression");
        TemplateExpression expr2 = mock("secondExpression");
        var expr3 = mock(TemplateExpression.class);

        when(expr1.apply(context))
                .thenReturn("Hello");
        when(expr2.apply(context))
                .thenReturn(", ");
        when(expr3.apply(context))
                .thenReturn("world");

        var concat = new TemplateConcatTemplateExpression(List.of(
                expr1,
                expr2,
                expr3
        ));

        var result = concat.apply(context);

        assertEquals("Hello, world", result);
    }

    @Test
    void applyShouldConvertNullResultsToStringLiteralNull() {
        Context context = mock();

        TemplateExpression expr1 = mock("firstExpression");
        TemplateExpression expr2 = mock("secondExpression");

        when(expr1.apply(context))
                .thenReturn(null);
        when(expr2.apply(context))
                .thenReturn(" suffix");

        var concat = new TemplateConcatTemplateExpression(List.of(expr1, expr2));

        var result = concat.apply(context);

        // StringBuilder.append(null) → "null"
        assertEquals("null suffix", result);
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<Object> visitor = mock();

        var concat = new TemplateConcatTemplateExpression(List.of());

        when(visitor.visit(concat))
                .thenReturn("visited");

        var result = concat.visit(visitor);

        assertEquals("visited", result);
        verify(visitor)
                .visit(concat);
    }

    @Test
    void equalsAndHashCodeShouldDependOnExpressions() {
        TemplateExpression expr1 = mock("firstExpression");
        TemplateExpression expr2 = mock("secondExpression");

        var a = new TemplateConcatTemplateExpression(List.of(expr1, expr2));
        var b = new TemplateConcatTemplateExpression(List.of(expr1, expr2));
        var c = new TemplateConcatTemplateExpression(List.of(expr2, expr1));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, c);
    }

    @Test
    void toStringShouldContainExpressions() {
        var expr = mock(TemplateExpression.class);
        var concat = new TemplateConcatTemplateExpression(List.of(expr));

        var text = concat.toString();

        assertTrue(text.contains("expressions"));
    }
}

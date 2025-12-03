package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 *
 * @author sibmaks
 */
class TernaryTemplateExpressionTest {

    @Test
    void shouldEvaluateThenTrueBranchWhenConditionIsTrue() {
        var context = Context.empty();

        var conditionExpression = mock(TemplateExpression.class);
        when(conditionExpression.apply(context))
                .thenReturn(true);

        var trueExpression = mock(TemplateExpression.class);
        when(trueExpression.apply(context))
                .thenReturn("YES");

        var falseExpression = mock(TemplateExpression.class);

        var ternary = new TernaryTemplateExpression(
                conditionExpression,
                trueExpression,
                falseExpression
        );

        var result = ternary.apply(context);

        assertEquals("YES", result);

        verify(trueExpression)
                .apply(context);
        verify(falseExpression, never())
                .apply(context);
    }

    @Test
    void shouldEvaluateThenFalseBranchWhenConditionIsFalse() {
        var context = Context.empty();

        var conditionExpression = mock(TemplateExpression.class);
        when(conditionExpression.apply(context))
                .thenReturn(false);

        var trueExpression = mock(TemplateExpression.class);
        var falseExpression = mock(TemplateExpression.class);
        when(falseExpression.apply(context))
                .thenReturn("NO");

        var ternary = new TernaryTemplateExpression(
                conditionExpression,
                trueExpression,
                falseExpression
        );

        var result = ternary.apply(context);

        assertEquals("NO", result);

        verify(falseExpression)
                .apply(context);
        verify(trueExpression, never())
                .apply(context);
    }

    @Test
    void shouldThrowWhenConditionIsNotBoolean() {
        var context = Context.empty();

        var conditionExpression = mock(TemplateExpression.class);
        when(conditionExpression.apply(context))
                .thenReturn("not boolean");

        var ternary = new TernaryTemplateExpression(
                conditionExpression,
                mock(TemplateExpression.class),
                mock(TemplateExpression.class)
        );

        var exception = assertThrows(IllegalStateException.class,
                () -> ternary.apply(context));

        assertTrue(exception.getMessage().contains("Cannot evaluate expression"));
    }

    @Test
    void shouldCallVisitorVisit() {
        TemplateExpressionVisitor<Object> visitor = mock();

        var conditionExpression = mock(TemplateExpression.class);
        var trueExpression = mock(TemplateExpression.class);
        var falseExpression = mock(TemplateExpression.class);

        var ternary = new TernaryTemplateExpression(
                conditionExpression,
                trueExpression,
                falseExpression
        );

        var expected = "visited";
        when(visitor.visit(ternary))
                .thenReturn(expected);

        var result = ternary.visit(visitor);

        assertEquals(expected, result);

        verify(visitor)
                .visit(ternary);
    }

    @Test
    void equalsAndHashCodeShouldBeCorrect() {
        var c1 = mock(TemplateExpression.class);
        var t1 = mock(TemplateExpression.class);
        var f1 = mock(TemplateExpression.class);

        var first = new TernaryTemplateExpression(c1, t1, f1);
        var second = new TernaryTemplateExpression(c1, t1, f1);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}

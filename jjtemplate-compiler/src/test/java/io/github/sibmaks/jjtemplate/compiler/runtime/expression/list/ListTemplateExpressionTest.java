package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class ListTemplateExpressionTest {

    @Test
    void applyShouldEvaluateAllElementsInOrder() {
        Context context = mock();

        ListElement el1 = mock("firstElement");
        ListElement el2 = mock("secondElement");

        var expression = new ListTemplateExpression(List.of(el1, el2));

        var result = expression.apply(context);

        assertInstanceOf(List.class, result);

        @SuppressWarnings("unchecked")
        var list = (List<Object>) result;

        verify(el1)
                .apply(eq(context), same(list));

        verify(el2)
                .apply(eq(context), same(list));
    }

    @Test
    void applyShouldThrowExceptionIfElementIsNull() {
        Context context = mock();

        var args = new ArrayList<ListElement>();
        args.add(null);
        var expression = new ListTemplateExpression(args);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> expression.apply(context)
        );

        assertEquals("object element must not be null", ex.getMessage());
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        ListElement el = mock();
        var expression = new ListTemplateExpression(List.of(el));

        when(visitor.visit(expression))
                .thenReturn("visited");

        var result = expression.visit(visitor);

        assertEquals("visited", result);

        verify(visitor)
                .visit(expression);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnElements() {
        ListElement el1 = mock("firstElement");
        ListElement el2 = mock("secondElement");

        var expr1 = new ListTemplateExpression(List.of(el1, el2));
        var expr2 = new ListTemplateExpression(List.of(el1, el2));
        var expr3 = new ListTemplateExpression(List.of(el1));

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());

        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1.hashCode(), expr3.hashCode());
    }
}

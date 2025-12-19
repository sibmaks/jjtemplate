package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class ObjectTemplateExpressionTest {

    @Test
    void applyShouldApplyAllElementsInOrder() {
        Context context = mock();

        ObjectElement el1 = mock("firstElement");
        ObjectElement el2 = mock("secondElement");

        var expression = new ObjectTemplateExpression(
                List.of(el1, el2)
        );

        var result = expression.apply(context);

        Map<String, Object> map = assertInstanceOf(Map.class, result);

        verify(el1)
                .apply(context, map);

        verify(el2)
                .apply(context, map);
    }

    @Test
    void applyShouldThrowExceptionIfElementIsNull() {
        Context context = mock();

        var elements = new ArrayList<ObjectElement>();
        elements.add(null);

        var expression = new ObjectTemplateExpression(elements);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> expression.apply(context)
        );

        assertEquals("object element must not be null", ex.getMessage());
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        ObjectElement element = mock();
        var expression = new ObjectTemplateExpression(
                List.of(element)
        );

        when(visitor.visit(expression))
                .thenReturn("visited");

        var result = expression.visit(visitor);

        assertEquals("visited", result);

        verify(visitor)
                .visit(expression);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnElements() {
        ObjectElement el1 = mock("firstElement");
        ObjectElement el2 = mock("secondElement");

        var expr1 = new ObjectTemplateExpression(List.of(el1, el2));
        var expr2 = new ObjectTemplateExpression(List.of(el1, el2));
        var expr3 = new ObjectTemplateExpression(List.of(el1));

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());

        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1.hashCode(), expr3.hashCode());
    }
}

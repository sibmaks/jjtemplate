package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class SpreadObjectElementTest {

    @Test
    void applyShouldPutAllEntriesFromMap() {
        Context context = mock();

        TemplateExpression expression = mock();

        var key1 = UUID.randomUUID().toString();
        var key2 = UUID.randomUUID().toString();
        var value1 = UUID.randomUUID().toString();
        var value2 = UUID.randomUUID().toString();

        Map<String, Object> source = new HashMap<>();
        source.put(key1, value1);
        source.put(key2, value2);

        when(expression.apply(context))
                .thenReturn(source);

        var element = new SpreadObjectElement(expression);

        Map<String, Object> target = new HashMap<>();

        element.apply(context, target);

        assertEquals(2, target.size());
        assertEquals(value1, target.get(key1));
        assertEquals(value2, target.get(key2));
    }

    @Test
    void applyShouldDoNothingIfSourceIsNull() {
        Context context = mock();

        TemplateExpression expression = mock();

        when(expression.apply(context))
                .thenReturn(null);

        var element = new SpreadObjectElement(expression);

        var existingKey = UUID.randomUUID().toString();
        var existingValue = UUID.randomUUID().toString();

        Map<String, Object> target = new HashMap<>();
        target.put(existingKey, existingValue);

        element.apply(context, target);

        assertEquals(1, target.size());
        assertEquals(existingValue, target.get(existingKey));
    }

    @Test
    void applyShouldThrowExceptionIfSourceIsNotMap() {
        Context context = mock();

        TemplateExpression expression = mock();

        var invalidValue = UUID.randomUUID().toString();

        when(expression.apply(context))
                .thenReturn(invalidValue);

        var element = new SpreadObjectElement(expression);

        Map<String, Object> target = new HashMap<>();

        var exception = assertThrows(IllegalArgumentException.class,
                () -> element.apply(context, target)
        );

        assertEquals("spread object expects Map, got: java.lang.String", exception.getMessage());
    }

    @Test
    void visitShouldDelegateToVisitor() {
        ObjectElementVisitor<String> visitor = mock();

        TemplateExpression expression = mock();
        var element = new SpreadObjectElement(expression);

        var visitResult = UUID.randomUUID().toString();

        when(visitor.visit(element))
                .thenReturn(visitResult);

        var result = element.visit(visitor);

        assertEquals(visitResult, result);

        verify(visitor)
                .visit(element);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnExpression() {
        TemplateExpression expr1 = mock("firstExpression");
        TemplateExpression expr2 = mock("secondExpression");

        var el1 = new SpreadObjectElement(expr1);
        var el2 = new SpreadObjectElement(expr1);
        var el3 = new SpreadObjectElement(expr2);

        assertEquals(el1, el2);
        assertEquals(el1.hashCode(), el2.hashCode());

        assertNotEquals(el1, el3);
        assertNotEquals(el1.hashCode(), el3.hashCode());
    }
}

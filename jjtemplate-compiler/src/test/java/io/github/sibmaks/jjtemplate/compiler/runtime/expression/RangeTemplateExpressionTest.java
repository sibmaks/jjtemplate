package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class RangeTemplateExpressionTest {

    @Test
    void applyShouldIterateOverNull() {
        Context context = mock();

        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression name = mock("name");

        when(source.apply(context))
                .thenReturn(null);

        var bodyItem = UUID.randomUUID().toString();
        when(body.apply(context))
                .thenReturn(bodyItem);

        var itemVariableName = UUID.randomUUID().toString();
        var indexVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .itemVariableName(itemVariableName)
                .indexVariableName(indexVariableName)
                .body(body)
                .name(name)
                .build();

        var result = expression.apply(context);

        assertNull(result);

        var never = never();
        verify(context, never)
                .in(any());

        verify(context, never)
                .out();
    }

    @Test
    void applyShouldIterateOverCollection() {
        Context context = mock();

        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression name = mock("name");

        var sourceItem = UUID.randomUUID().toString();
        when(source.apply(context))
                .thenReturn(List.of(sourceItem));

        var bodyItem = UUID.randomUUID().toString();
        when(body.apply(context))
                .thenReturn(bodyItem);

        var itemVariableName = UUID.randomUUID().toString();
        var indexVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .itemVariableName(itemVariableName)
                .indexVariableName(indexVariableName)
                .body(body)
                .name(name)
                .build();

        var result = expression.apply(context);

        assertEquals(List.of(bodyItem), result);

        ArgumentCaptor<Map<String, Object>> layerArgsCaptor = ArgumentCaptor.captor();
        verify(context)
                .in(layerArgsCaptor.capture());

        var layer = layerArgsCaptor.getValue();
        assertNotNull(layer);

        assertEquals(0, layer.get(indexVariableName));
        assertEquals(sourceItem, layer.get(itemVariableName));

        verify(context)
                .out();
    }

    @Test
    void applyShouldIterateOverArray() {
        Context context = mock();

        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression name = mock("name");

        var sourceItem = UUID.randomUUID().toString();
        when(source.apply(context))
                .thenReturn(new Object[]{sourceItem});

        var bodyItem = UUID.randomUUID().toString();
        when(body.apply(context))
                .thenReturn(bodyItem);

        var itemVariableName = UUID.randomUUID().toString();
        var indexVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .itemVariableName(itemVariableName)
                .indexVariableName(indexVariableName)
                .body(body)
                .name(name)
                .build();

        var result = expression.apply(context);

        assertEquals(List.of(bodyItem), result);

        ArgumentCaptor<Map<String, Object>> layerArgsCaptor = ArgumentCaptor.captor();
        verify(context)
                .in(layerArgsCaptor.capture());

        var layer = layerArgsCaptor.getValue();
        assertNotNull(layer);

        assertEquals(0, layer.get(indexVariableName));
        assertEquals(sourceItem, layer.get(itemVariableName));

        verify(context)
                .out();
    }

    @Test
    void applyShouldThrowExceptionForUnsupportedSource() {
        Context context = mock();

        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression name = mock("name");

        var value = UUID.randomUUID().toString();
        when(source.apply(context))
                .thenReturn(value);

        var itemVariableName = UUID.randomUUID().toString();
        var indexVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .itemVariableName(itemVariableName)
                .indexVariableName(indexVariableName)
                .body(body)
                .name(name)
                .build();

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> expression.apply(context)
        );

        assertEquals("Unsupported range source: " + value + ", " + value.getClass(), exception.getMessage());
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression separator = mock("name");

        var expression = new RangeTemplateExpression(
                source,
                "item",
                "index",
                body,
                separator
        );

        when(visitor.visit(expression))
                .thenReturn("visited");

        var result = expression.visit(visitor);

        assertEquals("visited", result);

        verify(visitor).visit(expression);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnFields() {
        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression separator = mock("name");

        var expr1 = new RangeTemplateExpression(source, "item", "index", body, separator);
        var expr2 = new RangeTemplateExpression(source, "item", "index", body, separator);
        var expr3 = new RangeTemplateExpression(source, "x", "index", body, separator);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());

        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1.hashCode(), expr3.hashCode());
    }
}

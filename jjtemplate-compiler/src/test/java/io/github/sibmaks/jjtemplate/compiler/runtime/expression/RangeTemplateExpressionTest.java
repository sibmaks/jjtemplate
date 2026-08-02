package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
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

        var firstVariableName = UUID.randomUUID().toString();
        var secondVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .firstVariableName(firstVariableName)
                .secondVariableName(secondVariableName)
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

        var firstVariableName = UUID.randomUUID().toString();
        var secondVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .firstVariableName(firstVariableName)
                .secondVariableName(secondVariableName)
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

        assertEquals(sourceItem, layer.get(firstVariableName));
        assertEquals(0, layer.get(secondVariableName));

        verify(context)
                .out();
    }

    @Test
    void applyShouldIterateOverMapKeyAndValueInSourceOrder() {
        Context context = mock();

        TemplateExpression source = mock("source");
        TemplateExpression body = mock("body");
        TemplateExpression name = mock("name");

        var sourceMap = new LinkedHashMap<String, Object>();
        sourceMap.put("first", 1);
        sourceMap.put("second", null);
        when(source.apply(context))
                .thenReturn(sourceMap);
        when(body.apply(context))
                .thenReturn("first-result", "second-result");

        var firstVariableName = UUID.randomUUID().toString();
        var secondVariableName = UUID.randomUUID().toString();
        var iterationScopes = new ArrayList<Map<String, Object>>();
        doAnswer(invocation -> {
            iterationScopes.add(new HashMap<>(invocation.getArgument(0)));
            return null;
        }).when(context).in(any());

        var expression = RangeTemplateExpression.builder()
                .source(source)
                .firstVariableName(firstVariableName)
                .secondVariableName(secondVariableName)
                .body(body)
                .name(name)
                .build();

        var result = expression.apply(context);

        assertEquals(List.of("first-result", "second-result"), result);
        assertEquals(2, iterationScopes.size());
        assertEquals("first", iterationScopes.get(0).get(firstVariableName));
        assertEquals(1, iterationScopes.get(0).get(secondVariableName));
        assertEquals("second", iterationScopes.get(1).get(firstVariableName));
        assertTrue(iterationScopes.get(1).containsKey(secondVariableName));
        assertNull(iterationScopes.get(1).get(secondVariableName));
        verify(context, times(2)).out();
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

        var firstVariableName = UUID.randomUUID().toString();
        var secondVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .firstVariableName(firstVariableName)
                .secondVariableName(secondVariableName)
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

        assertEquals(sourceItem, layer.get(firstVariableName));
        assertEquals(0, layer.get(secondVariableName));

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

        var firstVariableName = UUID.randomUUID().toString();
        var secondVariableName = UUID.randomUUID().toString();
        var expression = RangeTemplateExpression.builder()
                .source(source)
                .firstVariableName(firstVariableName)
                .secondVariableName(secondVariableName)
                .body(body)
                .name(name)
                .build();

        var exception = assertThrows(TemplateEvalException.class, () -> expression.apply(context));

        assertEquals("Failed execute: \"" + expression + "\"", exception.getMessage());
        assertEquals("Unsupported range source: " + value + ", " + value.getClass(), exception.getCause().getMessage());
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
                separator,
                null
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

        var expr1 = new RangeTemplateExpression(source, "item", "index", body, separator, null);
        var expr2 = new RangeTemplateExpression(source, "item", "index", body, separator, null);
        var expr3 = new RangeTemplateExpression(source, "x", "index", body, separator, null);

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());

        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1.hashCode(), expr3.hashCode());
    }
}

package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ListTemplateFunctionTest {
    @InjectMocks
    private ListTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("list", actual);
    }

    @Test
    void withoutArguments() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var list = assertInstanceOf(List.class, actual.getValue());
        assertTrue(list.isEmpty());
    }

    @Test
    void withSingleArgument() {
        var item = UUID.randomUUID().toString();
        var args = List.of(ExpressionValue.of(item));
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var list = assertInstanceOf(List.class, actual.getValue());
        assertFalse(list.isEmpty());
        assertEquals(item, list.get(0));
    }

    @Test
    void withSinglePipe() {
        var item = UUID.randomUUID().toString();
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(item);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var list = assertInstanceOf(List.class, actual.getValue());
        assertFalse(list.isEmpty());
        assertEquals(item, list.get(0));
    }

    @Test
    void withArgAndPipe() {
        var argItem = UUID.randomUUID().toString();
        var pipeItem = UUID.randomUUID().toString();
        var args = List.of(ExpressionValue.of(argItem));
        var pipe = ExpressionValue.of(pipeItem);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var list = assertInstanceOf(List.class, actual.getValue());
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
        assertEquals(argItem, list.get(0));
        assertEquals(pipeItem, list.get(1));
    }

}
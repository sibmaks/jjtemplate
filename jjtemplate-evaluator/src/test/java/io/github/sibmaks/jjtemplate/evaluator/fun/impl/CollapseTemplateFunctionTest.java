package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class CollapseTemplateFunctionTest {
    @Mock
    private TemplateEvaluator evaluator;
    @InjectMocks
    private CollapseTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("collapse", actual);
    }

    @Test
    void withoutArguments() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
        );
        assertEquals("collapse: at least 1 argument required", exception.getMessage());
    }

    @Test
    void singleMapAsArgument() {
        var map = Map.of("key", "value");
        var value = function.invoke(List.of(ExpressionValue.of(map)), ExpressionValue.empty());
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void singleMapAsPipe() {
        var map = Map.of("key", "value");
        var value = function.invoke(List.of(), ExpressionValue.of(map));
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void listOfSingleMapAsArgument() {
        var map = Map.of("key", "value");
        var list = List.of(map);
        var value = function.invoke(List.of(ExpressionValue.of(list)), ExpressionValue.empty());
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void listOfSingleMapAsPipe() {
        var map = Map.of("key", "value");
        var list = List.of(map);
        var value = function.invoke(List.of(), ExpressionValue.of(list));
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void listOfSingleObjectAsPipe() {
        var object = new Stub();
        object.field = UUID.randomUUID().toString();
        object.method = UUID.randomUUID().toString();
        var list = List.of(object);

        when(evaluator.getFields(Stub.class))
                .thenReturn(ReflectionUtils.scanFields(Stub.class));

        when(evaluator.getMethods(Stub.class))
                .thenReturn(scanMethods(Stub.class));

        var value = function.invoke(List.of(), ExpressionValue.of(list));
        assertFalse(value.isEmpty());
        assertEquals(
                Map.of(
                        "field", object.field,
                        "method", object.getMethod()
                ),
                value.getValue()
        );
    }

    @Test
    void objectAsPipe() {
        var object = new Stub();
        object.field = UUID.randomUUID().toString();
        object.method = UUID.randomUUID().toString();

        when(evaluator.getFields(Stub.class))
                .thenReturn(ReflectionUtils.scanFields(Stub.class));

        when(evaluator.getMethods(Stub.class))
                .thenReturn(scanMethods(Stub.class));

        var value = function.invoke(List.of(), ExpressionValue.of(object));
        assertFalse(value.isEmpty());
        assertEquals(
                Map.of(
                        "field", object.field,
                        "method", object.getMethod()
                ),
                value.getValue()
        );
    }

    static class Stub {
        public String field;
        private String method;

        public String getMethod() {
            return method + "-";
        }
    }

    private static Map<String, Method> scanMethods(Class<?> type) {
        var ignored = ReflectionUtils.scanMethods(Object.class);
        var typed = ReflectionUtils.scanMethods(type);
        for (var ignoredMethod : ignored.keySet()) {
            typed.remove(ignoredMethod);
        }
        return typed;
    }

}
package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class CollapseTemplateFunctionTest {
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

    @Test
    void childObjectAsPipe() {
        var object = new Child();
        object.field = UUID.randomUUID().toString();
        object.method = UUID.randomUUID().toString();
        object.child = UUID.randomUUID().toString();
        var list = List.of(object);

        var value = function.invoke(List.of(), ExpressionValue.of(list));
        assertFalse(value.isEmpty());
        assertEquals(
                Map.of(
                        "field", object.field,
                        "method", object.getMethod(),
                        "child", object.child
                ),
                value.getValue()
        );
    }

    static class Stub {
        public String field;
        protected String method;

        public String getMethod() {
            return method + "-";
        }
    }

    static class Child extends Stub {
        public String child;
    }

}
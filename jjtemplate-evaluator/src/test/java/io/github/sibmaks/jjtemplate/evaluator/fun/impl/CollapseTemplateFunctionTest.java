package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("collapse: at least 1 argument required", exception.getMessage());
    }

    @Test
    void singleMapAsArgument() {
        var map = Map.of("key", "value");
        var args = List.of(ExpressionValue.of(map));
        var pipe = ExpressionValue.empty();
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void withNullArguments() {
        var args = List.of(ExpressionValue.of(null));
        var pipe = ExpressionValue.of(null);
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(Map.of(), value.getValue());
    }

    @Test
    void singleMapAsPipe() {
        var args = List.<ExpressionValue>of();
        var map = Map.of("key", "value");
        var pipe = ExpressionValue.of(map);
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void listOfSingleMapAsArgument() {
        var map = Map.of("key", "value");
        var list = List.of(map);
        var args = List.of(ExpressionValue.of(list));
        var pipe = ExpressionValue.empty();
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void listOfSingleMapAsPipe() {
        var map = Map.of("key", "value");
        var list = List.of(map);
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(list);
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void listOfSingleObjectAsPipe() {
        var object = new Stub();
        object.field = UUID.randomUUID().toString();
        object.method = UUID.randomUUID().toString();
        var list = List.of(object);
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(list);

        var value = function.invoke(args, pipe);
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
    void arrayOfSingleMapAsArgument() {
        var map = Map.of("key", "value");
        var array = new Object[]{map};
        var args = List.of(ExpressionValue.of(array));
        var pipe = ExpressionValue.empty();
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void arrayOfSingleMapAsPipe() {
        var map = Map.of("key", "value");
        var array = new Object[]{map};
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(array);
        var value = function.invoke(args, pipe);
        assertFalse(value.isEmpty());
        assertEquals(map, value.getValue());
    }

    @Test
    void arrayOfSingleObjectAsPipe() {
        var object = new Stub();
        object.field = UUID.randomUUID().toString();
        object.method = UUID.randomUUID().toString();
        var array = new Object[]{object};
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(array);

        var value = function.invoke(args, pipe);
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
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(object);

        var value = function.invoke(args, pipe);
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
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.of(list);

        var value = function.invoke(args, pipe);
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
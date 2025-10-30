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
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ContainsTemplateFunctionTest {

    @InjectMocks
    private ContainsTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("contains", actual);
    }

    @Test
    void withoutArguments() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("contains: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void with1Argument() {
        var args = List.of(ExpressionValue.of(null));
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("contains: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void containsInList() {
        var value = UUID.randomUUID().hashCode();
        var arg = List.of(value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(value),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void containsInListAsPipe() {
        var value = UUID.randomUUID().hashCode();
        var arg = List.of(value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(value);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void notContainsInList() {
        var value = UUID.randomUUID().hashCode();
        var arg = List.of(value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(value + 1),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void notContainsInListAsPipe() {
        var value = UUID.randomUUID().hashCode();
        var arg = List.of(value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(value + 1);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void containsInObjectArray() {
        var value = UUID.randomUUID().hashCode();
        var arg = new Object[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(value),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void containsInObjectArrayAsPipe() {
        var value = UUID.randomUUID().hashCode();
        var arg = new Object[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(value);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void notContainsInObjectArray() {
        var value = UUID.randomUUID().hashCode();
        var arg = new Object[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(value + 1),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void notContainsInObjectArrayAsPipe() {
        var value = UUID.randomUUID().hashCode();
        var arg = new Object[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(value + 1);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void containsInPrimitiveArray() {
        var value = UUID.randomUUID().hashCode();
        var arg = new int[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(value),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void containsInPrimitiveArrayAsPipe() {
        var value = UUID.randomUUID().hashCode();
        var arg = new int[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(value);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void notContainsInPrimitiveArray() {
        var value = UUID.randomUUID().hashCode();
        var arg = new int[]{value};
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(value + 1)
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void notContainsInPrimitiveArrayAsPipe() {
        var value = UUID.randomUUID().hashCode();
        var arg = new int[]{value};
        var args = List.of(
                ExpressionValue.of(arg)
        );
        var pipe = ExpressionValue.of(value + 1);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void notContainsInNull() {
        var args = List.of(
                ExpressionValue.of(null)
        );
        var pipe = ExpressionValue.of(1);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void exceptionOnUnsupportedType() {
        var args = List.of(
                ExpressionValue.of(42)
        );
        var pipe = ExpressionValue.of(1);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, pipe));
        assertEquals("contains: first argument of unsupported type " + Integer.class, exception.getMessage());
    }

    @Test
    void containsInMap() {
        var key = UUID.randomUUID().toString();
        var value = UUID.randomUUID().hashCode();
        var arg = Map.of(key, value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(key),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void containsInMapAsPipe() {
        var key = UUID.randomUUID().toString();
        var value = UUID.randomUUID().hashCode();
        var arg = Map.of(key, value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(key);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void notContainsInMap() {
        var key = UUID.randomUUID().toString();
        var value = UUID.randomUUID().hashCode();
        var arg = Map.of(key, value);
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(key + 1)
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void notContainsInMapAsPipe() {
        var key = UUID.randomUUID().toString();
        var value = UUID.randomUUID().hashCode();
        var arg = Map.of(key, value);
        var args = List.of(
                ExpressionValue.of(arg)
        );
        var pipe = ExpressionValue.of(key + 1);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void containsInString() {
        var value = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(value),
                ExpressionValue.of(value.charAt(0)),
                ExpressionValue.of(value.charAt(1)),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void containsInStringAsPipe() {
        var value = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(value),
                ExpressionValue.of(value.charAt(0)),
                ExpressionValue.of(value.charAt(1)),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(value.charAt(2));
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(true, actual.getValue());
    }

    @Test
    void notContainsInString() {
        var value = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(value),
                ExpressionValue.of(value.charAt(0)),
                ExpressionValue.of(true)
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

    @Test
    void notContainsInStringAsPipe() {
        var value = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(value),
                ExpressionValue.of(value.charAt(0))
        );
        var pipe = ExpressionValue.of(true);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(false, actual.getValue());
    }

}
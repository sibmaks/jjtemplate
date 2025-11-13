package io.github.sibmaks.jjtemplate.evaluator.fun.impl.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class MapNewTemplateFunctionTest {
    @InjectMocks
    private MapNewTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("map", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("new", actual);
    }

    @Test
    void testInvokeWithPipeArg_evenArgs() {
        var args = List.<Object>of("a", 1, "b", 2);
        var pipeArg = "ignored";

        var result = function.invoke(args, pipeArg);

        assertEquals(2, result.size());
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
    }

    @Test
    void testInvokeWithPipeArg_oddArgs() {
        var args = List.<Object>of("a", 1, "b");
        var pipeArg = 99;

        var result = function.invoke(args, pipeArg);

        assertEquals(2, result.size());
        assertEquals(1, result.get("a"));
        assertEquals(99, result.get("b"));
    }

    @Test
    void testInvokeWithPipeArg_emptyArgs() {
        var args = List.of();
        var pipeArg = 42;

        var result = function.invoke(args, pipeArg);

        assertEquals(0, result.size());
    }

    @Test
    void testInvokeWithoutPipeArg_evenArgs() {
        var args = List.<Object>of("x", 10, "y", 20);

        var result = function.invoke(args);

        assertEquals(2, result.size());
        assertEquals(10, result.get("x"));
        assertEquals(20, result.get("y"));
    }

    @Test
    void testInvokeWithoutPipeArg_oddArgs() {
        var args = List.<Object>of("x", 10, "y");

        var result = function.invoke(args);

        assertEquals(1, result.size());
        assertEquals(10, result.get("x"));
        assertFalse(result.containsKey("y"));
    }

    @Test
    void testInvokeWithoutPipeArg_emptyArgs_shouldThrow() {
        var args = List.of();

        var exception = assertThrows(RuntimeException.class, () -> function.invoke(args));
        assertEquals("map:new: at least 1 argument required", exception.getMessage());
    }

}
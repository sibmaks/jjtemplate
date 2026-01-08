package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StringContainsTemplateFunctionTest {
    @InjectMocks
    private StringContainsTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @Test
    void checkFunctionName() {
        assertEquals("contains", function.getName());
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void stringContainsAllSubstrings() {
        var args = List.<Object>of("wor", "ld");
        var actual = function.invoke(args, "hello world");
        assertTrue(actual);
    }

    @Test
    void stringNotContainsSubstring() {
        var args = List.<Object>of("wo", "ZZZ");
        var actual = function.invoke(args, "hello world");
        assertFalse(actual);
    }

    @Test
    void collectionContainsAll() {
        var container = List.of(1, 2, 3);
        var args = List.of(container, 1, 2, 3);
        var actual = function.invoke(args);
        assertTrue(actual);
    }

    @Test
    void collectionNotContainsAll() {
        var container = List.of(1, 2);
        var args = List.of(container, 1, 2, 3);
        var actual = function.invoke(args);
        assertFalse(actual);
    }

    @Test
    void mapContainsAllKeys() {
        var map = Map.of("a", 1, "b", 2);
        var args = List.of(map, "a", "b");
        var actual = function.invoke(args);
        assertTrue(actual);
    }

    @Test
    void mapNotContainsKey() {
        var map = Map.of("a", 1);
        var args = List.of(map, "a", "b");
        var actual = function.invoke(args);
        assertFalse(actual);
    }

    @Test
    void stringContainsViaInvokeList() {
        var args = List.<Object>of("hello world", "hello");
        var actual = function.invoke(args);
        assertTrue(actual);
    }

    @Test
    void notEnoughArgsInInvoke() {
        var args = List.<Object>of("onlyOneArg");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:contains: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void notEnoughArgsInPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "text"));
        assertEquals("string:contains: at least 1 argument required", exception.getMessage());
    }

    @Test
    void mapContainsAllWithPipeInvoke() {
        var map = Map.of(1, "a", 2, "b");
        var args = List.<Object>of(1, 2);
        var actual = function.invoke(args, map);
        assertTrue(actual);
    }

    @Test
    void collectionContainsWithPipeInvoke() {
        var collection = Set.of("x", "y", "z");
        var args = List.<Object>of("x", "y");
        var actual = function.invoke(args, collection);
        assertTrue(actual);
    }

    @Test
    void stringContainsWithPipeInvoke() {
        var line = "abcdef";
        var args = List.<Object>of("a", "b", "f");
        var actual = function.invoke(args, line);
        assertTrue(actual);
    }

    @Test
    void stringDoesNotContainWithPipeInvoke() {
        var line = "abcdef";
        var args = List.<Object>of("a", "b", "z");
        var actual = function.invoke(args, line);
        assertFalse(actual);
    }

}
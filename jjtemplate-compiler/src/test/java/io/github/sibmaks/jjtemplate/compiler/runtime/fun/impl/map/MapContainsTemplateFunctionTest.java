package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.map;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class MapContainsTemplateFunctionTest {
    @InjectMocks
    private MapContainsTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("map", actual);
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
    void notEnoughArgsInInvoke() {
        var args = List.<Object>of("onlyOneArg");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("map:contains: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void notEnoughArgsInPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "text"));
        assertEquals("map:contains: at least 1 argument required", exception.getMessage());
    }

    @Test
    void unsupportedTypeThrows() {
        var args = List.<Object>of(1, 2);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 123.456));
        assertEquals("map:contains: 1st argument of unsupported type class java.lang.Double", exception.getMessage());
    }

    @Test
    void mapContainsAllWithPipeInvoke() {
        var map = Map.of(1, "a", 2, "b");
        var args = List.<Object>of(1, 2);
        var actual = function.invoke(args, map);
        assertTrue(actual);
    }

}
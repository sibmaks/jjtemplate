package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ListContainsTemplateFunctionTest {
    @InjectMocks
    private ListContainsTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("list", actual);
    }

    @Test
    void checkFunctionName() {
        assertEquals("contains", function.getName());
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
    void arrayContainsAll() {
        var array = new Integer[]{1, 2, 3};
        var args = List.<Object>of(array, 1, 2, 3);
        var actual = function.invoke(args);
        assertTrue(actual);
    }

    @Test
    void arrayNotContainsAll() {
        var array = new Integer[]{1, 2};
        var args = List.<Object>of(array, 1, 2, 3);
        var actual = function.invoke(args);
        assertFalse(actual);
    }

    @Test
    void notEnoughArgsInInvoke() {
        var args = List.<Object>of("onlyOneArg");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:contains: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void notEnoughArgsInPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "text"));
        assertEquals("list:contains: at least 1 argument required", exception.getMessage());
    }

    @Test
    void unsupportedTypeThrows() {
        var args = List.<Object>of(1, 2);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, 123.456));
        assertEquals("list:contains: 1st argument of unsupported type class java.lang.Double", exception.getMessage());
    }

    @Test
    void collectionContainsWithPipeInvoke() {
        var collection = Set.of("x", "y", "z");
        var args = List.<Object>of("x", "y");
        var actual = function.invoke(args, collection);
        assertTrue(actual);
    }

    @Test
    void arrayContainsWithPipeInvoke() {
        var array = new String[]{"a", "b", "c"};
        var args = List.<Object>of("a", "b");
        var actual = function.invoke(args, array);
        assertTrue(actual);
    }

}
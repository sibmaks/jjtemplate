package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ConcatTemplateFunctionTest {
    @InjectMocks
    private ConcatTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("concat", actual);
    }

    @Test
    void concatStrings() {
        var args = List.<Object>of("Hello", " ", "World", "!");
        var actual = function.invoke(args);
        assertEquals("Hello World!", actual);
    }

    @Test
    void concatStringsWithPipeArg() {
        var args = List.<Object>of("Base", "-", "End");
        var actual = function.invoke(args, "X");
        assertEquals("Base-EndX", actual);
    }

    @Test
    void concatCollections() {
        var list1 = List.of(1, 2);
        var list2 = List.of(3, 4);
        var list3 = List.of(5, 6);
        var args = List.<Object>of(list1, list2, list3);
        var actual = function.invoke(args);
        assertInstanceOf(List.class, actual);
        assertEquals(List.of(1, 2, 3, 4, 5, 6), actual);
    }

    @Test
    void concatCollectionsWithNestedArray() {
        var list1 = List.of("a", "b");
        var array = new String[]{"c", "d"};
        var list2 = List.of("e");
        var args = List.of(list1, array, list2);
        var actual = function.invoke(args);
        assertEquals(List.of("a", "b", "c", "d", "e"), actual);
    }

    @Test
    void concatArrays() {
        var first = new Integer[]{1, 2};
        var second = new Integer[]{3, 4};
        var args = List.<Object>of(first, second);
        var actual = function.invoke(args);
        assertEquals(List.of(1, 2, 3, 4), actual);
    }

    @Test
    void concatArrayWithCollectionAndSimpleValues() {
        var first = new Integer[]{1, 2};
        var list = List.of(3, 4);
        var args = List.of(first, list, 5, 6);
        var actual = function.invoke(args);
        assertEquals(List.of(1, 2, 3, 4, 5, 6), actual);
    }

    @Test
    void concatWithPipeArgCollection() {
        var list1 = List.of("x", "y");
        var args = List.of(list1, "z");
        var actual = function.invoke(args, "w");
        assertEquals(List.of("x", "y", "z", "w"), actual);
    }

    @Test
    void concatWithPipeArgArray() {
        var array = new String[]{"a", "b"};
        var args = List.<Object>of(array, "c");
        var actual = function.invoke(args, "d");
        assertEquals(List.of("a", "b", "c", "d"), actual);
    }

    @Test
    void concatWithPipeArgString() {
        var args = List.<Object>of("prefix", "_", "middle");
        var actual = function.invoke(args, "_suffix");
        assertEquals("prefix_middle_suffix", actual);
    }

    @Test
    void concatEmptyArgsThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("concat: at least 1 argument required", exception.getMessage());
    }

    @Test
    void concatEmptyArgsWithPipeThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "test"));
        assertEquals("concat: at least 1 argument required", exception.getMessage());
    }

    @Test
    void concatCollectionWithNestedCollectionAndArray() {
        var first = Set.of("a", "b");
        var nested = List.of("c");
        var array = new String[]{"d", "e"};
        var args = List.of(first, nested, array, "f");
        var actual = function.invoke(args);
        assertTrue(((List<?>) actual).containsAll(List.of("a", "b", "c", "d", "e", "f")));
    }

    @Test
    void concatArrayWithNestedCollections() {
        var first = new Integer[]{1, 2};
        var nested = List.of(3, 4);
        var nested2 = List.of(5);
        var args = List.of(first, nested, nested2);
        var actual = function.invoke(args);
        assertEquals(List.of(1, 2, 3, 4, 5), actual);
    }
}

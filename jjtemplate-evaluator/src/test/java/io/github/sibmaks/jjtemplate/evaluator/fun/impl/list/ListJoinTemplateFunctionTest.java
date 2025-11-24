package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ListJoinTemplateFunctionTest {
    @InjectMocks
    private ListJoinTemplateFunction function;


    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("list", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("join", actual);
    }

    @Test
    void joinCollections() {
        var list1 = List.of(1, 2);
        var list2 = List.of(3, 4);
        var list3 = List.of(5, 6);
        var args = List.of(" ", list1, list2, list3);
        var actual = function.invoke(args);
        assertEquals("1 2 3 4 5 6", actual);
    }

    @Test
    void joinCollectionsWithNull() {
        var list = List.of(1, 2);
        var args = new ArrayList<>();
        args.add(" ");
        args.add(null);
        args.add(list);
        var actual = function.invoke(args);
        assertEquals("null 1 2", actual);
    }

    @Test
    void joinCollectionsWithNestedArray() {
        var list1 = List.of("a", "b");
        var array = new String[]{"c", "d"};
        var list2 = List.of("e");
        var args = List.of(" ", list1, array, list2);
        var actual = function.invoke(args);
        assertEquals("a b c d e", actual);
    }

    @Test
    void joinArrays() {
        var first = new Integer[]{1, 2};
        var second = new Integer[]{3, 4};
        var args = List.<Object>of(", ", first, second);
        var actual = function.invoke(args);
        assertEquals("1, 2, 3, 4", actual);
    }

    @Test
    void joinArrayWithCollectionAndSimpleValues() {
        var first = new Integer[]{1, 2};
        var list = List.of(3, 4);
        var args = List.of(", ", first, list);
        var actual = function.invoke(args);
        assertEquals("1, 2, 3, 4", actual);
    }

    @Test
    void joinWithPipeArgCollection() {
        var list1 = List.of("x", "y");
        var args = List.of(" ", list1);
        var actual = function.invoke(args, List.of("w"));
        assertEquals("x y w", actual);
    }

    @Test
    void joinWithPipeArgArray() {
        var array = new String[]{"a", "b"};
        var args = List.<Object>of(", ", array);
        var actual = function.invoke(args, List.of("d"));
        assertEquals("a, b, d", actual);
    }

    @Test
    void joinEmptyArgsThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:join: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void joinEmptyArgsWithPipeThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "test"));
        assertEquals("list:join: at least 1 argument required", exception.getMessage());
    }

    @Test
    void joinArrayWithNestedCollections() {
        var first = new Integer[]{1, 2};
        var nested = List.of(3, 4);
        var nested2 = List.of(5);
        var args = List.of(", ", first, nested, nested2);
        var actual = function.invoke(args);
        assertEquals("1, 2, 3, 4, 5", actual);
    }
}
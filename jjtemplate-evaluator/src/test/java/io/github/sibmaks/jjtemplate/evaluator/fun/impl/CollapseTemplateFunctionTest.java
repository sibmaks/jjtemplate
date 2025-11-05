package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

/**
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
    void nullValueInGetProperties() {
        var args = new ArrayList<>();
        args.add(new Dummy());
        args.add(null);
        var actual = function.invoke(args);
        assertNotNull(actual);
    }

    @Test
    void getPropertiesFromCollection() {
        var dummy = new Dummy();
        dummy.name = UUID.randomUUID().toString();
        dummy.age = UUID.randomUUID().hashCode();

        var list = List.of(dummy, dummy);
        var args = List.of(list, dummy);

        var actual = function.invoke(args);
        assertTrue(actual.containsKey("name"));
        assertTrue(actual.containsKey("age"));
    }


    @Test
    void getPropertiesFromArray() {
        try (var mocked = mockStatic(ReflectionUtils.class)) {
            var dummyFields = new LinkedHashMap<String, Object>();
            dummyFields.put("x", 1);
            dummyFields.put("y", 2);
            mocked.when(() -> ReflectionUtils.getAllProperties(any()))
                    .thenReturn(dummyFields);

            var array = new Dummy[]{new Dummy(), new Dummy()};
            var args = List.of(array, new Dummy());
            var actual = function.invoke(args);
            assertTrue(actual.containsKey("x"));
            assertTrue(actual.containsKey("y"));
        }
    }

    @Test
    void mergePropertiesFromPipeAndArgs() {
        var dummy1 = new LinkedHashMap<String, Object>();
        dummy1.put("a", 1);
        var dummy2 = new LinkedHashMap<String, Object>();
        dummy2.put("b", 2);

        var args = List.<Object>of(dummy1);

        var result = function.invoke(args, dummy2);

        assertTrue(result.containsKey("a"));
        assertTrue(result.containsKey("b"));
    }

    @Test
    void notEnoughArgsOnPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, new Dummy()));
        assertEquals("collapse: at least 1 argument required", exception.getMessage());
    }

    @Test
    void notEnoughArgsOnInvoke() {
        var args = List.<Object>of(new Dummy());
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("collapse: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void mergeMultipleObjects() {
        var dummy1 = new Dummy();
        dummy1.name = UUID.randomUUID().toString();

        var dummy2 = new Dummy();
        dummy2.age = UUID.randomUUID().hashCode();

        var args = List.<Object>of(dummy1, dummy2);
        var actual = function.invoke(args);
        assertEquals(dummy2.name, actual.get("name"));
        assertEquals(dummy2.age, actual.get("age"));
    }

    static class Dummy {
        public String name;
        public int age;
    }
}

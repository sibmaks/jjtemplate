package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ListNewTemplateFunctionTest {
    @InjectMocks
    private ListNewTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("list", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("new", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list:new: at least 1 argument required", exception.getMessage());
    }

    @Test
    void simpleInvoke() {
        var item = UUID.randomUUID().toString();
        var args = List.<Object>of(item);
        var actual = function.invoke(args);
        assertEquals(args, actual);
    }

    @Test
    void pipeInvoke() {
        var item = UUID.randomUUID().toString();
        var pipe = UUID.randomUUID().toString();
        var args = List.<Object>of(item);
        var actual = function.invoke(args, pipe);
        var excepted = List.<Object>of(item, pipe);
        assertEquals(excepted, actual);
    }

}
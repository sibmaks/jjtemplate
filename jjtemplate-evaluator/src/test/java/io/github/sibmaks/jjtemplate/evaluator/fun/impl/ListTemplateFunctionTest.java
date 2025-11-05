package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
class ListTemplateFunctionTest {
    @InjectMocks
    private ListTemplateFunction function;

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("list: at least 1 argument required", exception.getMessage());
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
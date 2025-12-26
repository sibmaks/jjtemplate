package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.datetime;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateFunctionEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class DateTimeNowTemplateFunctionTest {
    @InjectMocks
    private DateTimeNowTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("datetime", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("now", actual);
    }

    @Test
    void isDynamic() {
        var actual = function.isDynamic();
        assertTrue(actual);
    }

    @Test
    void callAsPipeCauseException() {
        var args = List.of();
        var exception = assertThrows(TemplateFunctionEvalException.class, () -> function.invoke(args, null));
        assertEquals("datetime:now: too much arguments passed", exception.getMessage());
    }

    @Test
    void callAsArgsWithArgsPipeCauseException() {
        var args = List.of(new Object());
        var exception = assertThrows(TemplateFunctionEvalException.class, () -> function.invoke(args));
        assertEquals("datetime:now: too much arguments passed", exception.getMessage());
    }

    @Test
    void callAsArgs() {
        var args = List.of();
        var localDateTime = function.invoke(args);
        assertNotNull(localDateTime);
        assertEquals(LocalDate.now(), localDateTime.toLocalDate());
    }

}
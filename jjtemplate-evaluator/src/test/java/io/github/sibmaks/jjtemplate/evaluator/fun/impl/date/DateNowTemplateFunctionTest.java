package io.github.sibmaks.jjtemplate.evaluator.fun.impl.date;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateFunctionEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class DateNowTemplateFunctionTest {
    @InjectMocks
    private DateNowTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("date", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("now", actual);
    }

    @Test
    void callAsPipeCauseException() {
        var args = List.of();
        var exception = assertThrows(TemplateFunctionEvalException.class, () -> function.invoke(args, null));
        assertEquals("date:now: too much arguments passed", exception.getMessage());
    }

    @Test
    void callAsArgsWithArgsPipeCauseException() {
        var args = List.of(new Object());
        var exception = assertThrows(TemplateFunctionEvalException.class, () -> function.invoke(args));
        assertEquals("date:now: too much arguments passed", exception.getMessage());
    }

    @Test
    void callAsArgs() {
        var args = List.of();
        var localDate = function.invoke(args);
        assertEquals(LocalDate.now(), localDate);
    }

}
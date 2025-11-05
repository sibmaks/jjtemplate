package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class NotTemplateFunctionTest {
    @InjectMocks
    private NotTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("not", actual);
    }

    @Test
    void withoutArgument() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("not: 1 argument required", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("not: too much arguments passed", exception.getMessage());
    }

    @Test
    void notBooleanArgument() {
        var args = List.of();
        var pipe = 42;
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, pipe));
        assertEquals("not: argument must be a boolean", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void passAsArgument(boolean value) {
        var actual = function.invoke(List.of(value));
        assertEquals(!value, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void passAsPipe(boolean value) {
        var actual = function.invoke(List.of(), value);
        assertEquals(!value, actual);
    }

}
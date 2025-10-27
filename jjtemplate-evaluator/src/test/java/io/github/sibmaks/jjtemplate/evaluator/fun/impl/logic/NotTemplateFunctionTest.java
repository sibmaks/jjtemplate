package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 * @since 0.0.1
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
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
        );
        assertEquals("not: invalid args", exception.getMessage());
    }

    @Test
    void notBooleanArgument() {
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(List.of(), ExpressionValue.of(42))
        );
        assertEquals("not: arg not boolean", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void passAsArgument(boolean value) {
        var actual = function.invoke(List.of(ExpressionValue.of(value)), ExpressionValue.empty());
        assertFalse(actual.isEmpty());
        assertEquals(!value, actual.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void passAsPipe(boolean value) {
        var actual = function.invoke(List.of(), ExpressionValue.of(value));
        assertFalse(actual.isEmpty());
        assertEquals(!value, actual.getValue());
    }

}
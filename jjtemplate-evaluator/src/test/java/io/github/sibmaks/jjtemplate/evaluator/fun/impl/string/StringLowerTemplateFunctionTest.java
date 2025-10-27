package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class StringLowerTemplateFunctionTest {
    private final StringLowerTemplateFunction function = new StringLowerTemplateFunction(Locale.US);

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("lower", actual);
    }

    @Test
    void lowerWithoutArguments() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("lower: 1 argument required", exception.getMessage());
    }

    @Test
    void lower() {
        var cases = List.of(
                "Hello",
                42,
                true,
                3.1415
        );
        for (var aCase : cases) {
            var actual = function.invoke(List.of(), ExpressionValue.of(aCase));
            assertFalse(actual.isEmpty());
            assertEquals(aCase.toString().toLowerCase(), actual.getValue());
        }
    }

}
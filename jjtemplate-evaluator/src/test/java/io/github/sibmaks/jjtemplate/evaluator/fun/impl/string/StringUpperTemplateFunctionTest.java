package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class StringUpperTemplateFunctionTest {
    private final TemplateFunction function = new StringUpperTemplateFunction(Locale.US);

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("upper", actual);
    }

    @Test
    void upperWithoutArguments() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("upper: 1 argument required", exception.getMessage());
    }

    @Test
    void upper() {
        var cases = List.of(
                "Hello",
                42,
                true,
                3.1415
        );
        for (var aCase : cases) {
            var actual = function.invoke(List.of(), ExpressionValue.of(aCase));
            assertFalse(actual.isEmpty());
            assertEquals(aCase.toString().toUpperCase(), actual.getValue());
        }
    }

}
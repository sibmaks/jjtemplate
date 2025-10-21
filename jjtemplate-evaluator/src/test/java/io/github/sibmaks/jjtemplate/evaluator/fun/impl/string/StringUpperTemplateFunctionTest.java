package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class StringUpperTemplateFunctionTest {
    private final TemplateFunction function = new StringUpperTemplateFunction();

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("upper", actual);
    }

    @Test
    void upperWithoutArguments() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
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
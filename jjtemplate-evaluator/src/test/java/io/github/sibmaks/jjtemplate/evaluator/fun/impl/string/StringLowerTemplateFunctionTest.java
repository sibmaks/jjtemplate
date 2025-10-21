package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class StringLowerTemplateFunctionTest {
    private final StringLowerTemplateFunction function = new StringLowerTemplateFunction();

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("lower", actual);
    }

    @Test
    void lowerWithoutArguments() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> function.invoke(List.of(), ExpressionValue.empty())
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
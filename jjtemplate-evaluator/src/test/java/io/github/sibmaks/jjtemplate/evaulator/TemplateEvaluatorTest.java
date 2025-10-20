package io.github.sibmaks.jjtemplate.evaulator;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.parser.api.FunctionCallExpression;
import io.github.sibmaks.jjtemplate.parser.api.LiteralExpression;
import io.github.sibmaks.jjtemplate.parser.api.VariableExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author sibmaks
 */
class TemplateEvaluatorTest {

    @ParameterizedTest
    @ValueSource(booleans = {
            true,
            false
    })
    void checkBooleanLiteralExpression(boolean value) {
        var expression = new LiteralExpression(value);
        var context = mock(Context.class);

        var evaluator = new TemplateEvaluator();

        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(value, evaluated.getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {
            -42,
            0,
            42
    })
    void checkIntLiteralExpression(int value) {
        var expression = new LiteralExpression(value);
        var context = mock(Context.class);

        var evaluator = new TemplateEvaluator();

        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(value, evaluated.getValue());
    }

    @Test
    void checkVariableExpression() {
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        when(context.getRoot(varName))
                .thenReturn(ExpressionValue.of(varValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(varValue, evaluated.getValue());
    }

    @Test
    void checkVariableExpressionWhenNull() {
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        when(context.getRoot(varName))
                .thenReturn(ExpressionValue.empty());

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertTrue(evaluated.isEmpty());
        assertNull(evaluated.getValue());
    }

    @Test
    void checkPathVariableExpression() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var mapVarValue = Map.of(varName, varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(mapVarValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(varValue, evaluated.getValue());
    }

    @ParameterizedTest
    @MethodSource("callToStringFunctionCases")
    void callToStringFunction(Object value, String excepted) {
        var evaluator = new TemplateEvaluator();

        var expression = new FunctionCallExpression("str", List.of(
                new LiteralExpression(value)
        ));
        var evaluated = evaluator.evaluate(expression, mock());
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(excepted, evaluated.getValue());
    }

    @Test
    void callToStringFunctionWhenNoArgs() {
        var evaluator = new TemplateEvaluator();

        var expression = new FunctionCallExpression("str", List.of());
        var evaluated = evaluator.evaluate(expression, mock());
        assertNotNull(evaluated);
        assertTrue(evaluated.isEmpty());
    }

    public static Stream<Arguments> callToStringFunctionCases() {
        return Stream.of(
                Arguments.of(-42, "-42"),
                Arguments.of(0, "0"),
                Arguments.of(42, "42"),

                Arguments.of(-3.1415, "-3.1415"),
                Arguments.of(0.0, "0.0"),
                Arguments.of(3.1415, "3.1415"),

                Arguments.of(true, "true"),
                Arguments.of(false, "false")
        );
    }
}
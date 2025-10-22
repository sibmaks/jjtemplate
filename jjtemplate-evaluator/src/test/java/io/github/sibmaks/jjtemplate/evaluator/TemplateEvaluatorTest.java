package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.parser.api.FunctionCallExpression;
import io.github.sibmaks.jjtemplate.parser.api.LiteralExpression;
import io.github.sibmaks.jjtemplate.parser.api.PipeExpression;
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
    void emptyPathVariableExpression() {
        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of());
        var evaluated = evaluator.evaluate(expression, mock());
        assertNotNull(evaluated);
        assertTrue(evaluated.isEmpty());
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
    void checkVariableExpressionWhenNotExisted() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.empty());

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertTrue(evaluated.isEmpty());
        assertNull(evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnMap() {
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

    @Test
    void checkPathVariableExpressionOnList() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "0";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var listVarValue = List.of(varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(listVarValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(varValue, evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnListWhenNonInt() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "0a";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var listVarValue = List.of(varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(listVarValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Unknown property '%s' for %s", varName, listVarValue.getClass()), exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnListOutOfIndex() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = Integer.toString(1);
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var listVarValue = List.of(varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(listVarValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("List index out of range: %s", varName), exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnArray() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "0";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var arrayVarValue = new String[]{varValue};
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(arrayVarValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(varValue, evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnArrayOutOfIndex() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = Integer.toString(1);
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var arrayVarValue = new String[]{varValue};
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(arrayVarValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Array index out of range: %s", 1), exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnString() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "0";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(varValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(Character.toString(varValue.charAt(0)), evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnStringOutOfIndex() {
        var parentVarName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var varName = Integer.toString(varValue.length());
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(varValue));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("String index out of range: %s", varName), exception.getMessage());
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

    @Test
    void simplePipeExpression() {
        var evaluator = new TemplateEvaluator();

        var leftExpression = new LiteralExpression("true");
        var rightExpression = new FunctionCallExpression("boolean", List.of());
        var pipeExpression = new PipeExpression(
                leftExpression,
                List.of(rightExpression)
        );
        var evaluated = evaluator.evaluate(pipeExpression, mock());
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(true, evaluated.getValue());
    }

    @Test
    void notExistedFunctionExpression() {
        var evaluator = new TemplateEvaluator();

        var expression = new FunctionCallExpression("var", List.of());
        var exception = assertThrows(TemplateEvalException.class, () -> evaluator.evaluate(expression, mock()));
        assertEquals("Function 'var' not found", exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnField() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "field";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var object = new Stub();
        object.field = varValue;
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(object));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(varValue, evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnMethod() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "property";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var object = new Stub();
        object.property = varValue;
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(object));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(varValue + "-", evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnMethodBooleanObject() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "booleanObject";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var object = new Stub();
        object.property = varValue;
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(object));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(Boolean.TRUE, evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnMethodBooleanPrimitive() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "booleanPrimitive";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var object = new Stub();
        object.property = varValue;
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(object));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertFalse(evaluated.isEmpty());
        assertEquals(Boolean.TRUE, evaluated.getValue());
    }

    @Test
    void checkPathVariableExpressionOnNoExistedProperty() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "notExisted";
        var context = mock(Context.class);
        var object = new Stub();
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(object));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Unknown property '%s' for %s", varName, object.getClass()), exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnMethodWithException() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "exception";
        var context = mock(Context.class);
        var object = new Stub();
        when(context.getRoot(parentVarName))
                .thenReturn(ExpressionValue.of(object));

        var evaluator = new TemplateEvaluator();

        var expression = new VariableExpression(List.of(parentVarName, varName));
        var exception = assertThrows(RuntimeException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Error invoking getter '%s' on %s", varName, object.getClass()), exception.getMessage());
    }

    static class Stub {
        public String field;
        private String property;

        public String getProperty() {
            return property + "-";
        }

        public Boolean isBooleanObject() {
            return Boolean.TRUE;
        }

        public boolean isBooleanPrimitive() {
            return true;
        }

        public String getException() {
            throw new RuntimeException("internal");
        }
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
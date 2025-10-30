package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.parser.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;
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

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(value, evaluated);
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

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(value, evaluated);
    }

    @Test
    void checkVariableExpression() {
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        when(context.getRoot(varName))
                .thenReturn(varValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(varValue, evaluated);
    }

    @Test
    void emptyPathVariableExpression() {
        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(List.of());
        var evaluated = evaluator.evaluate(expression, mock());
        assertNull(evaluated);
    }

    @Test
    void checkVariableExpressionWhenNull() {
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        when(context.getRoot(varName))
                .thenReturn(null);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNull(evaluated);
    }

    @Test
    void checkVariableExpressionWhenNotExisted() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        when(context.getRoot(parentVarName))
                .thenReturn(null);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNull(evaluated);
    }

    @Test
    void checkPathVariableExpressionOnMap() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var mapVarValue = Map.of(varName, varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(mapVarValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(varValue, evaluated);
    }

    @Test
    void checkPathVariableExpressionOnList() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "0";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var listVarValue = List.of(varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(listVarValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(varValue, evaluated);
    }

    @Test
    void checkPathVariableExpressionOnListWhenNonInt() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "0a";
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var listVarValue = List.of(varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(listVarValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Unknown property '%s' of %s", varName, listVarValue.getClass()), exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnListOutOfIndex() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = Integer.toString(1);
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var listVarValue = List.of(varValue);
        when(context.getRoot(parentVarName))
                .thenReturn(listVarValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
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
                .thenReturn(arrayVarValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(varValue, evaluated);
    }

    @Test
    void checkPathVariableExpressionOnArrayOutOfIndex() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = Integer.toString(1);
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var arrayVarValue = new String[]{varValue};
        when(context.getRoot(parentVarName))
                .thenReturn(arrayVarValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
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
                .thenReturn(varValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(Character.toString(varValue.charAt(0)), evaluated);
    }

    @Test
    void checkPathVariableExpressionOnStringOutOfIndex() {
        var parentVarName = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var varValue = UUID.randomUUID().toString();
        var varName = Integer.toString(varValue.length());
        when(context.getRoot(parentVarName))
                .thenReturn(varValue);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("String index out of range: %s", varName), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("callToStringFunctionCases")
    void callToStringFunction(Object value, String excepted) {
        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new FunctionCallExpression("str", List.of(
                new LiteralExpression(value)
        ));
        var evaluated = evaluator.evaluate(expression, mock());
        assertNotNull(evaluated);
        assertEquals(excepted, evaluated);
    }

    @Test
    void simplePipeExpression() {
        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var leftExpression = new LiteralExpression("true");
        var rightExpression = new FunctionCallExpression("boolean", List.of());
        var pipeExpression = new PipeExpression(
                leftExpression,
                List.of(rightExpression)
        );
        var evaluated = evaluator.evaluate(pipeExpression, mock());
        assertNotNull(evaluated);
        assertEquals(true, evaluated);
    }

    @Test
    void notExistedFunctionExpression() {
        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new FunctionCallExpression("var", List.of());
        var context = mock(Context.class);
        var exception = assertThrows(TemplateEvalException.class, () -> evaluator.evaluate(expression, context));
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
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(varValue, evaluated);
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
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(varValue + "-", evaluated);
    }

    @Test
    void checkPathVariableExpressionOnMethodWithArg() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "callMethodWithArg";
        var argValue = UUID.randomUUID().toString();
        var context = mock(Context.class);
        var object = new Stub();
        when(context.getRoot(parentVarName))
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName, List.of(
                                new LiteralExpression(argValue)
                        ))
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals("-" + argValue, evaluated);
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
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(Boolean.TRUE, evaluated);
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
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals(Boolean.TRUE, evaluated);
    }

    @Test
    void checkPathVariableExpressionOnNoExistedProperty() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "notExisted";
        var context = mock(Context.class);
        var object = new Stub();
        when(context.getRoot(parentVarName))
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Unknown property '%s' of %s", varName, object.getClass()), exception.getMessage());
    }

    @Test
    void checkPathVariableExpressionOnMethodWithException() {
        var parentVarName = UUID.randomUUID().toString();
        var varName = "exception";
        var context = mock(Context.class);
        var object = new Stub();
        when(context.getRoot(parentVarName))
                .thenReturn(object);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var expression = new VariableExpression(
                List.of(
                        new VariableExpression.Segment(parentVarName),
                        new VariableExpression.Segment(varName)
                )
        );
        var exception = assertThrows(RuntimeException.class, () -> evaluator.evaluate(expression, context));
        assertEquals(String.format("Failed to access property '%s' of %s", varName, object.getClass()), exception.getMessage());
        var cause = exception.getCause();
        assertNotNull(cause);
        assertEquals("internal", cause.getMessage());
    }

    @Test
    void checkTernaryOperatorLeft() {
        var expression = new TernaryExpression(
                new LiteralExpression(true),
                new LiteralExpression("ok"),
                new LiteralExpression("fail")
        );
        var context = mock(Context.class);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals("ok", evaluated);
    }

    @Test
    void checkTernaryOperatorRight() {
        var expression = new TernaryExpression(
                new LiteralExpression(false),
                new LiteralExpression("fail"),
                new LiteralExpression("ok")
        );
        var context = mock(Context.class);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var evaluated = evaluator.evaluate(expression, context);
        assertNotNull(evaluated);
        assertEquals("ok", evaluated);
    }

    @Test
    void checkTernaryOperatorNotBooleanExpression() {
        var expression = new TernaryExpression(
                new LiteralExpression(42),
                new LiteralExpression("fail"),
                new LiteralExpression("fail")
        );
        var context = mock(Context.class);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var exception = assertThrows(
                TemplateEvalException.class,
                () -> evaluator.evaluate(expression, context)
        );
        assertEquals("cond must be a boolean: " + 42, exception.getMessage());
    }

    @Test
    void checkNotSupportedExpression() {
        var expression = new NotSupportedExpression();
        var context = mock(Context.class);

        var options = TemplateEvaluationOptions.builder()
                .locale(Locale.US)
                .build();
        var evaluator = new TemplateEvaluator(options);

        var exception = assertThrows(
                TemplateEvalException.class,
                () -> evaluator.evaluate(expression, context)
        );
        assertEquals("Unknown expr type: " + NotSupportedExpression.class, exception.getMessage());
    }

    static class NotSupportedExpression implements Expression {
        @Override
        public <R> R accept(ExpressionVisitor<R> visitor) {
            return null;
        }
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

        public String callMethodWithArg(String arg) {
            return "-" + arg;
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
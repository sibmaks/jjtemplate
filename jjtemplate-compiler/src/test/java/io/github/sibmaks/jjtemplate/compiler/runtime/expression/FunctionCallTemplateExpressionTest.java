package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class FunctionCallTemplateExpressionTest {

    @Test
    void applyShouldInvokeFunctionWithEvaluatedArguments() {
        var context = Context.empty();

        var arg1 = mock(TemplateExpression.class);
        var arg2 = mock(TemplateExpression.class);

        when(arg1.apply(context))
                .thenReturn("A");

        when(arg2.apply(context))
                .thenReturn("B");

        TemplateFunction<String> function = mock();

        when(function.invoke(List.of("A", "B")))
                .thenReturn("AB");

        var expression = new FunctionCallTemplateExpression(
                function,
                List.of(arg1, arg2)
        );

        var result = expression.apply(context);

        assertEquals("AB", result);

        verify(function)
                .invoke(List.of("A", "B"));
    }

    @Test
    void applyWithPipeShouldInvokeFunctionPassingPipeValue() {
        var context = Context.empty();

        var arg1 = mock(TemplateExpression.class);
        var arg2 = mock(TemplateExpression.class);

        when(arg1.apply(context))
                .thenReturn(10);

        when(arg2.apply(context))
                .thenReturn(20);

        TemplateFunction<Integer> function = mock();

        when(function.invoke(List.of(10, 20), 100))
                .thenReturn(130);

        var expression = new FunctionCallTemplateExpression(
                function,
                List.of(arg1, arg2)
        );

        var result = expression.apply(context, 100);

        assertEquals(130, result);

        verify(function)
                .invoke(List.of(10, 20), 100);
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        var function = mock(TemplateFunction.class);
        var expression = new FunctionCallTemplateExpression(
                function,
                List.of()
        );

        when(visitor.visit(expression))
                .thenReturn("visited");

        var result = expression.visit(visitor);

        assertEquals("visited", result);

        verify(visitor)
                .visit(expression);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnFunctionAndArguments() {
        var functionA = mock(TemplateFunction.class);
        var functionB = mock(TemplateFunction.class);

        var arg = mock(TemplateExpression.class);

        var expr1 = new FunctionCallTemplateExpression(functionA, List.of(arg));
        var expr2 = new FunctionCallTemplateExpression(functionA, List.of(arg));
        var expr3 = new FunctionCallTemplateExpression(functionB, List.of(arg));

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());

        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1.hashCode(), expr3.hashCode());
    }
}

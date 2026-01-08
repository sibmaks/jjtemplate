package io.github.sibmaks.jjtemplate.compiler.runtime.expression.function;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class DynamicFunctionCallTemplateExpressionTest {

    @Test
    void applyShouldInvokeFunctionWithEvaluatedArguments() {
        Context context = mock();

        ListTemplateExpression args = mock();

        List<Object> list = List.of(UUID.randomUUID());
        when(args.apply(context))
                .thenReturn(list);

        TemplateFunction<String> function = mock();

        var functionResult = UUID.randomUUID().toString();
        when(function.invoke(list))
                .thenReturn(functionResult);

        var expression = new DynamicFunctionCallTemplateExpression(
                function,
                args
        );

        var result = expression.apply(context);

        assertEquals(functionResult, result);

        verify(function)
                .invoke(list);
    }

    @Test
    void applyWithPipeShouldInvokeFunctionPassingPipeValue() {
        Context context = mock();

        ListTemplateExpression args = mock();

        List<Object> list = List.of(UUID.randomUUID());
        when(args.apply(context))
                .thenReturn(list);

        TemplateFunction<String> function = mock();

        var functionResult = UUID.randomUUID().toString();
        var pipe = UUID.randomUUID().hashCode();
        when(function.invoke(list, pipe))
                .thenReturn(functionResult);

        var expression = new DynamicFunctionCallTemplateExpression(
                function,
                args
        );

        var result = expression.apply(context, pipe);

        assertEquals(functionResult, result);

        verify(function)
                .invoke(list, pipe);
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        var function = mock(TemplateFunction.class);
        var expression = new DynamicFunctionCallTemplateExpression(
                function,
                mock()
        );

        when(visitor.visit(expression))
                .thenReturn("visited");

        var result = expression.visit(visitor);

        assertEquals("visited", result);

        verify(visitor)
                .visit(expression);
    }

}

package io.github.sibmaks.jjtemplate.compiler.runtime.expression.function;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
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
class ConstantFunctionCallTemplateExpressionTest {

    @Test
    void applyArgumentsIsStatic() {
        Context context = mock();

        List<Object> list = List.of(UUID.randomUUID());

        TemplateFunction<String> function = mock();

        var functionResult = UUID.randomUUID().toString();
        when(function.invoke(list))
                .thenReturn(functionResult);

        var expression = new ConstantFunctionCallTemplateExpression(
                function,
                list
        );

        var result = expression.apply(context);

        assertEquals(functionResult, result);

        verify(function)
                .invoke(list);
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        var function = mock(TemplateFunction.class);
        var expression = new ConstantFunctionCallTemplateExpression(
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
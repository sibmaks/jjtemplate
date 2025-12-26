package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.FunctionCallTemplateExpression;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;


/**
 *
 * @author sibmaks
 */
class PipeChainTemplateExpressionTest {

    @Test
    void applyShouldEvaluateRootAndSequentiallyInvokePipeFunctions() {
        var context = mock(Context.class);

        var root = mock(TemplateExpression.class);
        DynamicFunctionCallTemplateExpression pipe1 = mock("pipe1");
        DynamicFunctionCallTemplateExpression pipe2 = mock("pipe2");

        when(root.apply(context))
                .thenReturn("start");

        when(pipe1.apply(context, "start"))
                .thenReturn("middle");

        when(pipe2.apply(context, "middle"))
                .thenReturn("end");

        var chain = List.<FunctionCallTemplateExpression>of(pipe1, pipe2);
        var expression = new PipeChainTemplateExpression(root, chain);

        var result = expression.apply(context);

        assertEquals("end", result);

        verify(root).apply(context);
        verify(pipe1).apply(context, "start");
        verify(pipe2).apply(context, "middle");
    }

    @Test
    void applyShouldReturnRootValueWhenChainIsEmpty() {
        var context = mock(Context.class);

        var root = mock(TemplateExpression.class);
        when(root.apply(context))
                .thenReturn("only");

        var expression = new PipeChainTemplateExpression(root, List.of());

        var result = expression.apply(context);

        assertEquals("only", result);
        verify(root).apply(context);
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<Object> visitor = mock();
        var root = mock(TemplateExpression.class);
        var chain = List.<FunctionCallTemplateExpression>of();

        var expression = new PipeChainTemplateExpression(root, chain);

        when(visitor.visit(expression))
                .thenReturn("visited");

        var result = expression.visit(visitor);

        assertEquals("visited", result);
        verify(visitor).visit(expression);
    }

    @Test
    void equalsAndHashCodeShouldMatchForIdenticalExpressions() {
        var root = mock(TemplateExpression.class);

        DynamicFunctionCallTemplateExpression pipe = mock();

        var exp1 = new PipeChainTemplateExpression(root, List.of(pipe));
        var exp2 = new PipeChainTemplateExpression(root, List.of(pipe));

        assertEquals(exp1, exp2);
        assertEquals(exp1.hashCode(), exp2.hashCode());
    }

    @Test
    void equalsShouldFailForDifferentStructure() {
        var rootA = mock(TemplateExpression.class);
        var rootB = mock(TemplateExpression.class);

        var exp1 = new PipeChainTemplateExpression(rootA, List.of());
        var exp2 = new PipeChainTemplateExpression(rootB, List.of());

        assertNotEquals(exp1, exp2);
    }
}

package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.varusage.VariableUsageCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class VariableUsageCollectorTest {
    @InjectMocks
    private VariableUsageCollector collector;

    @Test
    void visitValueTemplateExpression_doesNothing() {
        var expr = mock(ConstantTemplateExpression.class);

        collector.visit(expr);

        assertTrue(collector.getVariables().isEmpty());
        verifyNoInteractions(expr);
    }

    @Test
    void visitVariableTemplateExpression_addsName() {
        var expr = mock(VariableTemplateExpression.class);
        when(expr.getRootName())
                .thenReturn("x");

        collector.visit(expr);

        assertEquals(Set.of("x"), collector.getVariables());
        verify(expr)
                .getRootName();
    }

    @Test
    void visitDynamicFunctionCallTemplateExpression_collectsArgs() {
        ListTemplateExpression argsExpression = mock();

        var func = mock(DynamicFunctionCallTemplateExpression.class);
        when(func.getArgExpression())
                .thenReturn(argsExpression);

        collector.visit(func);

        verify(argsExpression)
                .visit(collector);
    }

    @Test
    void visitConstantFunctionCallTemplateExpression_collectsArgs() {
        var func = mock(ConstantFunctionCallTemplateExpression.class);

        try {
            collector.visit(func);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void visitPipeChainTemplateExpression_collectsRootAndArgs() {
        var root = mock(TemplateExpression.class);

        var chainCall = mock(DynamicFunctionCallTemplateExpression.class);

        var pipe = mock(PipeChainTemplateExpression.class);
        when(pipe.getRoot())
                .thenReturn(root);
        when(pipe.getChain())
                .thenReturn(List.of(chainCall));

        collector.visit(pipe);

        verify(root)
                .visit(collector);
        verify(chainCall)
                .visit(collector);
    }

    @Test
    void visitTemplateConcatTemplateExpression_collectsAllItems() {
        var e1 = mock(TemplateExpression.class);
        var e2 = mock(TemplateExpression.class);
        var e3 = mock(TemplateExpression.class);

        var concat = mock(TemplateConcatTemplateExpression.class);
        when(concat.getExpressions())
                .thenReturn(List.of(e1, e2, e3));

        collector.visit(concat);

        verify(e1)
                .visit(collector);
        verify(e2)
                .visit(collector);
        verify(e3)
                .visit(collector);
    }

    @Test
    void visitTernaryTemplateExpression_collectsAllBranches() {
        var cond = mock(TemplateExpression.class);
        var thenTrue = mock(TemplateExpression.class);
        var thenFalse = mock(TemplateExpression.class);

        var ternary = mock(TernaryTemplateExpression.class);
        when(ternary.getCondition())
                .thenReturn(cond);
        when(ternary.getThenTrue())
                .thenReturn(thenTrue);
        when(ternary.getThenFalse())
                .thenReturn(thenFalse);

        collector.visit(ternary);

        verify(cond)
                .visit(collector);
        verify(thenTrue)
                .visit(collector);
        verify(thenFalse)
                .visit(collector);
    }

    @Test
    void multipleVariablesAreCollected() {
        var variable1 = mock(VariableTemplateExpression.class);
        var variable2 = mock(VariableTemplateExpression.class);
        when(variable1.getRootName())
                .thenReturn("a");
        when(variable2.getRootName())
                .thenReturn("b");

        collector.visit(variable1);
        collector.visit(variable2);

        assertEquals(Set.of("a", "b"), collector.getVariables());
    }

    @Test
    void duplicateVariablesAreIgnored() {
        var variable = mock(VariableTemplateExpression.class);
        when(variable.getRootName())
                .thenReturn("x");

        collector.visit(variable);
        collector.visit(variable);

        assertEquals(1, collector.getVariables().size());
        assertEquals(Set.of("x"), collector.getVariables());
    }
}
package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class VariableTemplateExpressionTest {
    @Mock
    private Context context;

    @Test
    void applyShouldReturnRootValueWhenNoChain() {
        var rootValue = "abc";

        when(context.getRoot("x"))
                .thenReturn(rootValue);

        var expression = new VariableTemplateExpression("x", List.of());

        var result = expression.apply(context);

        assertEquals(rootValue, result);
    }

    @Test
    void applyShouldStopOnNullRoot() {
        when(context.getRoot("x"))
                .thenReturn(null);

        var expression = new VariableTemplateExpression("x", List.of(
                new VariableTemplateExpression.GetPropertyChain("y")
        ));

        var result = expression.apply(context);

        assertNull(result);
    }

    @Test
    void applyShouldReturnNullWhenRootIsNull() {
        when(context.getRoot("missing"))
                .thenReturn(null);

        var expression = new VariableTemplateExpression("missing", List.of());

        var result = expression.apply(context);

        assertNull(result);
    }

    @Test
    void applyShouldFollowGetPropertyChain() {
        var rootValue = new Object();
        var propertyValue = "prop";

        when(context.getRoot("x"))
                .thenReturn(rootValue);

        try (MockedStatic<ReflectionUtils> utilities = mockStatic(ReflectionUtils.class)) {
            utilities.when(() -> ReflectionUtils.getProperty(rootValue, "name"))
                    .thenReturn(propertyValue);

            var chain = List.<VariableTemplateExpression.Chain>of(
                    new VariableTemplateExpression.GetPropertyChain("name")
            );
            var expression = new VariableTemplateExpression("x", chain);

            var result = expression.apply(context);

            assertEquals(propertyValue, result);
        }
    }

    @Test
    void applyShouldReturnNullIfGetPropertyChainProducesNull() {
        var rootValue = new Object();

        when(context.getRoot("x"))
                .thenReturn(rootValue);

        try (MockedStatic<ReflectionUtils> utilities = mockStatic(ReflectionUtils.class)) {
            utilities.when(() -> ReflectionUtils.getProperty(rootValue, "missing"))
                    .thenReturn(null);

            var chain = List.<VariableTemplateExpression.Chain>of(
                    new VariableTemplateExpression.GetPropertyChain("missing")
            );
            var expression = new VariableTemplateExpression("x", chain);

            var result = expression.apply(context);

            assertNull(result);
        }
    }

    @Test
    void applyShouldFollowCallMethodChain() {
        var rootValue = new Object();
        var argExpression = mock(TemplateExpression.class);
        var argValue = 123;
        var resultValue = 456;

        when(context.getRoot("x"))
                .thenReturn(rootValue);

        when(argExpression.apply(context))
                .thenReturn(argValue);

        try (MockedStatic<ReflectionUtils> utilities = mockStatic(ReflectionUtils.class)) {
            utilities.when(() -> ReflectionUtils.invokeMethodReflective(rootValue, "compute", List.of(argValue)))
                    .thenReturn(resultValue);

            var chain = List.<VariableTemplateExpression.Chain>of(
                    new VariableTemplateExpression.CallMethodChain("compute", List.of(argExpression))
            );

            var expression = new VariableTemplateExpression("x", chain);

            var result = expression.apply(context);

            assertEquals(resultValue, result);
        }
    }

    @Test
    void applyShouldStopIfIntermediateChainResultIsNull() {
        var rootValue = new Object();

        when(context.getRoot("x"))
                .thenReturn(rootValue);

        try (MockedStatic<ReflectionUtils> utilities = mockStatic(ReflectionUtils.class)) {
            utilities.when(() -> ReflectionUtils.getProperty(rootValue, "first"))
                    .thenReturn(null);

            var chain = List.<VariableTemplateExpression.Chain>of(
                    new VariableTemplateExpression.GetPropertyChain("first")
            );
            var expression = new VariableTemplateExpression("x", chain);

            var result = expression.apply(context);

            assertNull(result);
        }
    }

    @Test
    void visitShouldDelegateToVisitor() {
        var expression = new VariableTemplateExpression("x", List.of());
        var expected = "visited";

        TemplateExpressionVisitor<Object> visitor = mock();

        when(visitor.visit(expression))
                .thenReturn(expected);

        var result = expression.visit(visitor);

        assertEquals(expected, result);
    }
}

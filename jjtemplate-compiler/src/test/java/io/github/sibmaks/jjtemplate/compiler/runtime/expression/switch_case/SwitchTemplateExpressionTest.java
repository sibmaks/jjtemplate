package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpressionVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
class SwitchTemplateExpressionTest {

    @Test
    void applyShouldReturnFirstMatchingCaseResult() {
        Context context = mock();

        var conditionValue = UUID.randomUUID().toString();
        var resultValue = UUID.randomUUID().toString();

        TemplateExpression switchKey = mock("switchKey");
        TemplateExpression condition = mock("condition");
        when(condition.apply(context))
                .thenReturn(conditionValue);

        SwitchCase case1 = mock("firstCase");
        when(case1.matches(conditionValue, context))
                .thenReturn(false);

        SwitchCase case2 = mock("secondCase");
        when(case2.matches(conditionValue, context))
                .thenReturn(true);
        when(case2.evaluate(context, conditionValue))
                .thenReturn(resultValue);

        var expression = new SwitchTemplateExpression(
                switchKey,
                condition,
                List.of(case1, case2)
        );

        var result = expression.apply(context);

        assertEquals(resultValue, result);

        verify(case1)
                .matches(conditionValue, context);
        verify(case2)
                .matches(conditionValue, context);
        verify(case2)
                .evaluate(context, conditionValue);
    }

    @Test
    void applyShouldReturnNullIfNoCaseMatches() {
        Context context = mock();

        var conditionValue = UUID.randomUUID().toString();

        TemplateExpression switchKey = mock("switchKey");
        TemplateExpression condition = mock("condition");
        when(condition.apply(context))
                .thenReturn(conditionValue);

        SwitchCase case1 = mock("firstCase");
        SwitchCase case2 = mock("secondCase");

        when(case1.matches(any(), any()))
                .thenReturn(false);
        when(case2.matches(any(), any()))
                .thenReturn(false);

        var expression = new SwitchTemplateExpression(
                switchKey,
                condition,
                List.of(case1, case2)
        );

        var result = expression.apply(context);

        assertNull(result);
    }

    @Test
    void applyShouldThrowExceptionIfCaseIsNull() {
        Context context = mock();

        TemplateExpression switchKey = mock("switchKey");
        TemplateExpression condition = mock("condition");

        var cases = new java.util.ArrayList<SwitchCase>();
        cases.add(null);

        var expression = new SwitchTemplateExpression(
                switchKey,
                condition,
                cases
        );

        var ex = assertThrows(IllegalArgumentException.class,
                () -> expression.apply(context)
        );

        assertEquals("switch case must not be null", ex.getMessage());
    }

    @Test
    void visitShouldDelegateToVisitor() {
        TemplateExpressionVisitor<String> visitor = mock();

        TemplateExpression switchKey = mock("switchKey");
        TemplateExpression condition = mock("condition");

        var expression = new SwitchTemplateExpression(
                switchKey,
                condition,
                List.of()
        );

        var visitResult = UUID.randomUUID().toString();

        when(visitor.visit(expression))
                .thenReturn(visitResult);

        var result = expression.visit(visitor);

        assertEquals(visitResult, result);

        verify(visitor)
                .visit(expression);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnConditionAndCases() {
        TemplateExpression switchKey = mock("switchKey");
        TemplateExpression condition = mock("condition");

        SwitchCase case1 = mock("firstCase");
        SwitchCase case2 = mock("secondCase");

        var expr1 = new SwitchTemplateExpression(switchKey, condition, List.of(case1, case2));
        var expr2 = new SwitchTemplateExpression(switchKey, condition, List.of(case1, case2));
        var expr3 = new SwitchTemplateExpression(switchKey, condition, List.of(case1));

        assertEquals(expr1, expr2);
        assertEquals(expr1.hashCode(), expr2.hashCode());

        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1.hashCode(), expr3.hashCode());
    }
}

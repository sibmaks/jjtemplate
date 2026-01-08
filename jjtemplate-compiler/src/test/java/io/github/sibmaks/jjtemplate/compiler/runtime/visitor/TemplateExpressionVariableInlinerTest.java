package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner.TemplateExpressionVariableInliner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sibmaks
 */
final class TemplateExpressionVariableInlinerTest {

    @Test
    void variableNotInValuesReturnsSameExpression() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        var expression = new VariableTemplateExpression("unknown", List.of());

        var result = expression.visit(inliner);
        assertSame(expression, result);
    }

    @Test
    void variableExistsWithNullValueReturnsNullValueExpression() {
        var values = new HashMap<String, Object>();
        var variableName = UUID.randomUUID().toString();
        values.put(variableName, null);
        var inliner = new TemplateExpressionVariableInliner(values);

        var expression = new VariableTemplateExpression(variableName, List.of());

        var result = expression.visit(inliner);
        var inlined = assertInstanceOf(ConstantTemplateExpression.class, result);
        assertNull(inlined.getValue());
    }

    @Test
    void variableInlinedWithoutCallChain() {
        var values = new HashMap<String, Object>();
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().toString();
        values.put(variableName, variableValue);
        var inliner = new TemplateExpressionVariableInliner(values);

        var expression = new VariableTemplateExpression(variableName, List.of());

        var result = expression.visit(inliner);
        assertEquals(new ConstantTemplateExpression(variableValue), result);
    }

    @Test
    void variableWithPropertyChain() {
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().hashCode();
        var bean = new Object() {
            public final int value = variableValue;
        };

        var values = new HashMap<String, Object>();
        values.put(variableName, bean);
        var inliner = new TemplateExpressionVariableInliner(values);

        List<VariableTemplateExpression.Chain> chain =
                List.of(new VariableTemplateExpression.GetPropertyChain("value"));

        var expression = new VariableTemplateExpression(variableName, chain);

        var result = expression.visit(inliner);
        assertEquals(new ConstantTemplateExpression(variableValue), result);
    }

    @Test
    void variableWithMethodChain() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        List<VariableTemplateExpression.Chain> chain =
                List.of(new VariableTemplateExpression.CallMethodChain("method", List.of()));

        var expression = new VariableTemplateExpression("x", chain);

        var result = expression.visit(inliner);
        assertSame(expression, result);
    }

    @Test
    void methodChainIsAppliedWhenAllArgsInlineToValues() {
        var values = new HashMap<String, Object>();
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().toString();
        values.put(variableName, variableValue);
        var inliner = new TemplateExpressionVariableInliner(values);

        var arg = new ConstantTemplateExpression(2);

        List<VariableTemplateExpression.Chain> chain = List.of(
                new VariableTemplateExpression.CallMethodChain(
                        "substring",
                        List.of(arg)
                )
        );

        var expression = new VariableTemplateExpression(variableName, chain);

        var result = expression.visit(inliner);
        assertEquals(new ConstantTemplateExpression(variableValue.substring(2)), result);
    }

    @Test
    void functionCallInliner() {
        var values = new HashMap<String, Object>();
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().hashCode();
        values.put(variableName, variableValue);
        var inliner = new TemplateExpressionVariableInliner(values);

        ListTemplateExpression args = mock("args");
        ListTemplateExpression inlinedArg = mock("inlinedArg");
        when(args.visit(inliner))
                .thenReturn(inlinedArg);
        var expression = new DynamicFunctionCallTemplateExpression(null, args);

        var result = expression.visit(inliner);

        assertNotSame(expression, result);
        var inlined = assertInstanceOf(DynamicFunctionCallTemplateExpression.class, result);
        var actualInlinedArg = inlined.getArgExpression();
        assertEquals(inlinedArg, actualInlinedArg);
    }

    @Test
    void pipeChainInliner() {
        var values = new HashMap<String, Object>();
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().hashCode();
        values.put(variableName, variableValue);
        var inliner = new TemplateExpressionVariableInliner(values);

        var root = new VariableTemplateExpression(variableName, List.of());

        ListTemplateExpression args = mock("args");
        ListTemplateExpression inlinedArg = mock("inlinedArg");
        when(args.visit(inliner))
                .thenReturn(inlinedArg);
        var fcall = new DynamicFunctionCallTemplateExpression(null, args);

        var expression = new PipeChainTemplateExpression(root, List.of(fcall));

        var result = expression.visit(inliner);

        assertNotSame(expression, result);
        var inlined = assertInstanceOf(PipeChainTemplateExpression.class, result);
        assertEquals(new ConstantTemplateExpression(variableValue), inlined.getRoot());
    }

    @Test
    void templateConcatInliner() {
        var values = new HashMap<String, Object>();
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().hashCode();
        values.put(variableName, variableValue);
        var inliner = new TemplateExpressionVariableInliner(values);

        var concat = new TemplateConcatTemplateExpression(
                List.of(
                        new ConstantTemplateExpression("A"),
                        new VariableTemplateExpression(variableName, List.of()),
                        new ConstantTemplateExpression("B")
                )
        );

        var result = concat.visit(inliner);

        var inlined = assertInstanceOf(TemplateConcatTemplateExpression.class, result);
        var list = inlined.getExpressions();
        assertEquals("A", assertInstanceOf(ConstantTemplateExpression.class, list.get(0)).getValue());
        assertEquals(variableValue, assertInstanceOf(ConstantTemplateExpression.class, list.get(1)).getValue());
        assertEquals("B", assertInstanceOf(ConstantTemplateExpression.class, list.get(2)).getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ternaryExpressionInliner(boolean conditionValue) {
        var values = new HashMap<String, Object>();
        var condVariableName = UUID.randomUUID().toString();
        values.put(condVariableName, conditionValue);
        var variableName = UUID.randomUUID().toString();
        var variableValue = UUID.randomUUID().hashCode();
        values.put(variableName, variableValue);
        var inliner = new TemplateExpressionVariableInliner(values);

        var condition = new VariableTemplateExpression(condVariableName, List.of());
        var thenTrue = new VariableTemplateExpression(variableName, List.of());
        var thenFalse = new VariableTemplateExpression("unknown", List.of());

        var expression = new TernaryTemplateExpression(condition, thenTrue, thenFalse);

        var result = expression.visit(inliner);

        assertNotSame(expression, result);
        var t = (TernaryTemplateExpression) result;

        assertEquals(new ConstantTemplateExpression(conditionValue), t.getCondition());
        assertEquals(new ConstantTemplateExpression(variableValue), t.getThenTrue());
        assertSame(thenFalse, t.getThenFalse());
    }

    @Test
    void valueTemplateExpressionReturnsSame() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        var expression = new ConstantTemplateExpression("x");

        assertSame(expression, expression.visit(inliner));
    }

    @Test
    void functionCallReturnsSameWhenNoArgInlined() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        ListTemplateExpression args = mock();
        when(args.visit(inliner))
                .thenReturn(args);

        var functionCall = new DynamicFunctionCallTemplateExpression(
                mock(TemplateFunction.class),
                args
        );

        var result = functionCall.visit(inliner);

        assertSame(functionCall, result);
    }

    @Test
    void pipeChainReturnsSameWhenRootNotInlinedAndNoChainInlined() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        var root = new ConstantTemplateExpression("root");

        var functionCall = new ConstantFunctionCallTemplateExpression(
                mock(),
                List.of()
        );

        var pipe = new PipeChainTemplateExpression(root, List.of(functionCall));

        var result = pipe.visit(inliner);

        assertSame(pipe, result);
    }

    @Test
    void concatReturnsSameWhenNoExpressionInlined() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        var expr1 = new ConstantTemplateExpression("A");
        var expr2 = new ConstantTemplateExpression("B");

        var concat = new TemplateConcatTemplateExpression(List.of(expr1, expr2));

        var result = concat.visit(inliner);

        assertSame(concat, result);
    }

    @Test
    void ternaryReturnsSameWhenNoPartInlined() {
        var values = new HashMap<String, Object>();
        var inliner = new TemplateExpressionVariableInliner(values);

        var condition = new ConstantTemplateExpression(true);
        var thenTrue = new ConstantTemplateExpression("yes");
        var thenFalse = new ConstantTemplateExpression("no");

        var ternary = new TernaryTemplateExpression(condition, thenTrue, thenFalse);

        var result = ternary.visit(inliner);

        assertSame(ternary, result);
    }

}

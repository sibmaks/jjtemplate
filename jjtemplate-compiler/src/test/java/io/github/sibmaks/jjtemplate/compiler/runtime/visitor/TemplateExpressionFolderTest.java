package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder.TemplateExpressionFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class TemplateExpressionFolderTest {
    @InjectMocks
    private TemplateExpressionFolder folder;

    @Test
    void foldStaticFunctionWithConstantArgs() {
        TemplateFunction<String> function = mock();

        var arg1 = UUID.randomUUID().toString();
        var arg2 = UUID.randomUUID().hashCode();
        var value = UUID.randomUUID().toString();
        when(function.invoke(List.of(arg1, arg2)))
                .thenReturn(value);

        ListTemplateExpression baseArgsExpression = mock();
        ConstantTemplateExpression constantArgsExpression = mock();
        when(baseArgsExpression.visit(folder))
                .thenReturn(constantArgsExpression);
        when(constantArgsExpression.getValue())
                .thenReturn(List.of(arg1, arg2));

        var expr = new DynamicFunctionCallTemplateExpression(
                function,
                baseArgsExpression
        );

        var folded = folder.visit(expr);

        var foldedValue = assertInstanceOf(ConstantTemplateExpression.class, folded);
        assertEquals(value, foldedValue.getValue());
    }

    @Test
    void foldStaticFunction_oneConstantArg() {
        TemplateFunction<String> function = mock();

        ListTemplateExpression baseArgsExpression = mock("baseArgs");
        ListTemplateExpression foldedArgsExpression = mock("foldedArgs");
        when(baseArgsExpression.visit(folder))
                .thenReturn(foldedArgsExpression);

        var expr = new DynamicFunctionCallTemplateExpression(
                function,
                baseArgsExpression
        );

        var folded = folder.visit(expr);

        var foldedFunction = assertInstanceOf(DynamicFunctionCallTemplateExpression.class, folded);
        assertNotSame(expr, folded);

        var argExpression = foldedFunction.getArgExpression();
        assertSame(argExpression, foldedArgsExpression);
    }

    @Test
    void doNotFoldStaticFunctionWhenArgsNotConstant() {
        TemplateFunction<String> function = mock();

        ListTemplateExpression argsExpression = mock();
        when(argsExpression.visit(folder))
                .thenReturn(argsExpression);

        var expr = new DynamicFunctionCallTemplateExpression(
                function,
                argsExpression
        );

        var folded = folder.visit(expr);

        assertSame(expr, folded);
    }

    @Test
    void doNotFoldDynamicFunctionEvenIfConstantArgs() {
        TemplateFunction<String> function = mock();
        when(function.isDynamic())
                .thenReturn(true);

        var expr = new ConstantFunctionCallTemplateExpression(
                function,
                List.of()
        );

        var folded = folder.visit(expr);

        assertSame(expr, folded);
    }

    @Test
    void foldPipeAllStatic() {
        TemplateFunction<String> function = mock();

        var pipeArg = UUID.randomUUID().toString();
        var arg = UUID.randomUUID().toString();
        var value = UUID.randomUUID().toString();
        when(function.invoke(List.of(arg), pipeArg))
                .thenReturn(value);

        var pipe = new PipeChainTemplateExpression(
                new ConstantTemplateExpression(pipeArg),
                List.of(
                        new ConstantFunctionCallTemplateExpression(
                                function,
                                List.of(arg)
                        )
                )
        );

        var result = folder.visit(pipe);
        var folded = assertInstanceOf(ConstantTemplateExpression.class, result);
        assertEquals(value, folded.getValue());
    }

    @Test
    void foldPipe_oneConstantArg() {
        TemplateFunction<String> function = mock();

        ListTemplateExpression baseArgsExpression = mock("baseArgs");
        ListTemplateExpression foldedArgsExpression = mock("foldedArgs");
        when(baseArgsExpression.visit(folder))
                .thenReturn(foldedArgsExpression);

        var expr = new PipeChainTemplateExpression(
                new DynamicFunctionCallTemplateExpression(
                        function,
                        baseArgsExpression
                ),
                List.of()
        );

        var folded = folder.visit(expr);

        var foldedPipe = assertInstanceOf(PipeChainTemplateExpression.class, folded);
        assertNotSame(expr, folded);

        var foldedPipeRoot = assertInstanceOf(DynamicFunctionCallTemplateExpression.class, foldedPipe.getRoot());

        var argExpression = foldedPipeRoot.getArgExpression();
        assertSame(argExpression, foldedArgsExpression);
    }

    @Test
    void doNotFoldPipe() {
        TemplateFunction<String> function = mock();
        when(function.isDynamic())
                .thenReturn(true);

        var expr = new PipeChainTemplateExpression(
                new ConstantFunctionCallTemplateExpression(
                        function,
                        List.of()
                ),
                List.of()
        );

        var folded = folder.visit(expr);
        assertSame(expr, folded);
    }

    @Test
    void pipeStopsOnDynamicFunction() {
        TemplateFunction<String> staticked = mock();

        var pipeArg = UUID.randomUUID().toString();
        var arg = UUID.randomUUID().toString();
        var value = UUID.randomUUID().toString();
        when(staticked.invoke(List.of(arg), pipeArg))
                .thenReturn(value);

        TemplateFunction<String> dynamicked = mock();
        when(dynamicked.isDynamic())
                .thenReturn(true);

        var pipe = new PipeChainTemplateExpression(
                new ConstantTemplateExpression(pipeArg),
                List.of(
                        new ConstantFunctionCallTemplateExpression(staticked, List.of(arg)),
                        new ConstantFunctionCallTemplateExpression(dynamicked, List.of(2))
                )
        );

        var result = folder.visit(pipe);

        var folded = assertInstanceOf(PipeChainTemplateExpression.class, result);
        var foldedRoot = assertInstanceOf(ConstantTemplateExpression.class, folded.getRoot());
        assertEquals(value, foldedRoot.getValue());

        var chain = folded.getChain();
        assertEquals(1, chain.size());
        assertEquals(dynamicked, chain.get(0).getFunction());
    }

    @Test
    void pipeStopsWhenArgsNotConstant() {
        TemplateFunction<String> firstFunction = mock();

        var pipeArg = "baseRoot:" + UUID.randomUUID();
        var arg = "arg:" + UUID.randomUUID();
        var value = "newRoot:" + UUID.randomUUID();
        when(firstFunction.invoke(List.of(arg), pipeArg))
                .thenReturn(value);

        TemplateFunction<String> secondFunction = mock();

        ListTemplateExpression args2Expression = mock("args2Expression");

        var pipe = new PipeChainTemplateExpression(
                new ConstantTemplateExpression(pipeArg),
                List.of(
                        new ConstantFunctionCallTemplateExpression(firstFunction, List.of(arg)),
                        new DynamicFunctionCallTemplateExpression(secondFunction, args2Expression)
                )
        );

        var result = folder.visit(pipe);

        var folded = assertInstanceOf(PipeChainTemplateExpression.class, result);
        var foldedRoot = assertInstanceOf(ConstantTemplateExpression.class, folded.getRoot());

        assertEquals(value, foldedRoot.getValue());

        var chain = folded.getChain();
        assertEquals(1, chain.size());
        assertEquals(secondFunction, chain.get(0).getFunction());
    }

    @Test
    void foldConcatMergeStrings() {
        var left = UUID.randomUUID().toString();
        var right = UUID.randomUUID().toString();

        var concat = new TemplateConcatTemplateExpression(
                List.of(
                        new ConstantTemplateExpression(left),
                        new ConstantTemplateExpression(right)
                )
        );

        var result = folder.visit(concat);
        var folded = assertInstanceOf(ConstantTemplateExpression.class, result);
        assertEquals(left + right, folded.getValue());
    }

    @Test
    void concatWithMixedExpressions() {
        var concat = new TemplateConcatTemplateExpression(
                List.of(
                        new ConstantTemplateExpression(UUID.randomUUID().toString()),
                        new VariableTemplateExpression("x", List.of()),
                        new ConstantTemplateExpression(UUID.randomUUID().toString())
                )
        );

        var folded = assertInstanceOf(TemplateConcatTemplateExpression.class, folder.visit(concat));

        var list = folded.getExpressions();

        assertEquals(3, list.size());
        assertInstanceOf(ConstantTemplateExpression.class, list.get(0));
        assertInstanceOf(VariableTemplateExpression.class, list.get(1));
        assertInstanceOf(ConstantTemplateExpression.class, list.get(2));
    }

    @Test
    void concatNoChangesReturnsSameInstance() {
        var concat = new TemplateConcatTemplateExpression(
                List.of(
                        new VariableTemplateExpression("x", List.of()),
                        new VariableTemplateExpression("y", List.of())
                )
        );

        var folded = folder.visit(concat);
        assertSame(concat, folded);
    }

    @Test
    void concatPartialFold() {
        TemplateFunction<String> staticked = mock();

        var value = UUID.randomUUID().toString();
        when(staticked.invoke(List.of()))
                .thenReturn(value);

        var concat = new TemplateConcatTemplateExpression(
                List.of(
                        new VariableTemplateExpression("x", List.of()),
                        new DynamicFunctionCallTemplateExpression(staticked, new ListTemplateExpression(List.of()))
                )
        );

        var folded = assertInstanceOf(TemplateConcatTemplateExpression.class, folder.visit(concat));
        assertNotSame(concat, folded);

        var expressions = folded.getExpressions();
        var arg2 = assertInstanceOf(ConstantTemplateExpression.class, expressions.get(1));
        assertEquals(value, arg2.getValue());
    }

    @Test
    void foldTernaryTrue() {
        var thenTrue = "thenTrue" + UUID.randomUUID();
        var thenFalse = "thenFalse" + UUID.randomUUID();

        var ternary = new TernaryTemplateExpression(
                new ConstantTemplateExpression(true),
                new ConstantTemplateExpression(thenTrue),
                new ConstantTemplateExpression(thenFalse)
        );

        var folded = assertInstanceOf(ConstantTemplateExpression.class, folder.visit(ternary));
        assertEquals(thenTrue, folded.getValue());
    }

    @Test
    void foldTernaryFalse() {
        var thenTrue = "thenTrue" + UUID.randomUUID();
        var thenFalse = "thenFalse" + UUID.randomUUID();

        var ternary = new TernaryTemplateExpression(
                new ConstantTemplateExpression(false),
                new ConstantTemplateExpression(thenTrue),
                new ConstantTemplateExpression(thenFalse)
        );

        var folded = assertInstanceOf(ConstantTemplateExpression.class, folder.visit(ternary));
        assertEquals(thenFalse, folded.getValue());
    }

    @Test
    void ternaryNoFoldWhenConditionNotConstant() {
        var thenTrue = "thenTrue" + UUID.randomUUID();
        var thenFalse = "thenFalse" + UUID.randomUUID();

        var ternary = new TernaryTemplateExpression(
                new VariableTemplateExpression("x", List.of()),
                new ConstantTemplateExpression(thenTrue),
                new ConstantTemplateExpression(thenFalse)
        );

        var folded = folder.visit(ternary);
        assertSame(ternary, folded);
    }

    @Test
    void ternaryPartialFoldWhenSomeOfConditionConstant() {
        var thenTrue = "thenTrue" + UUID.randomUUID();
        var thenFalse = "thenFalse" + UUID.randomUUID();

        var ternary = new TernaryTemplateExpression(
                new VariableTemplateExpression("x", List.of()),
                new TemplateConcatTemplateExpression(List.of(new ConstantTemplateExpression(thenTrue))),
                new ConstantTemplateExpression(thenFalse)
        );

        var folded = assertInstanceOf(TernaryTemplateExpression.class, folder.visit(ternary));
        assertNotSame(ternary, folded);

        var foldedThenTrue = assertInstanceOf(ConstantTemplateExpression.class, folded.getThenTrue());
        assertEquals(thenTrue, foldedThenTrue.getValue());
    }

    @Test
    void noFoldVariableChain() {
        var v = new VariableTemplateExpression("x", List.of());
        assertSame(v, folder.visit(v));
    }

    @Test
    void foldVariableCallMethodArgs() {
        var call = new VariableTemplateExpression.CallMethodChain(
                "m",
                List.of(
                        new ConstantTemplateExpression("a"),
                        new ConstantTemplateExpression("b")
                )
        );

        var varExpr = new VariableTemplateExpression(
                "x",
                List.of(call)
        );

        var folded = folder.visit(varExpr);

        assertSame(varExpr, folded);
    }

    @Test
    void foldVariableCallMethodArgs_partialFold() {
        TemplateFunction<String> staticked = mock();

        var value = UUID.randomUUID().toString();
        when(staticked.invoke(List.of()))
                .thenReturn(value);

        var staticArg = new ConstantTemplateExpression("a");
        var call = new VariableTemplateExpression.CallMethodChain(
                "m",
                List.of(
                        staticArg,
                        new DynamicFunctionCallTemplateExpression(staticked, new ListTemplateExpression(List.of()))
                )
        );

        var varExpr = new VariableTemplateExpression(
                "x",
                List.of(call)
        );

        var folded = assertInstanceOf(VariableTemplateExpression.class, folder.visit(varExpr));

        assertNotSame(varExpr, folded);

        var newChain = folded.getCallChain();
        assertEquals(1, newChain.size());
        var newCall = assertInstanceOf(VariableTemplateExpression.CallMethodChain.class, newChain.get(0));

        assertEquals("m", newCall.getMethodName());

        var argsExpressions = newCall.getArgsExpressions();
        var foldedArg1 = assertInstanceOf(ConstantTemplateExpression.class, argsExpressions.get(0));
        assertEquals(staticArg, foldedArg1);
        var foldedArg2 = assertInstanceOf(ConstantTemplateExpression.class, argsExpressions.get(1));
        assertEquals(value, foldedArg2.getValue());
    }

    @Test
    void foldVariableGetProperty_doNothing() {
        var getPropertyChain = new VariableTemplateExpression.GetPropertyChain(
                "m"
        );

        var varExpr = new VariableTemplateExpression(
                "x",
                List.of(getPropertyChain)
        );

        var folded = assertInstanceOf(VariableTemplateExpression.class, folder.visit(varExpr));

        assertSame(varExpr, folded);
    }

    @Test
    void visitValueReturnsSame() {
        var expression = new ConstantTemplateExpression(123);
        assertSame(expression, folder.visit(expression));
    }
}
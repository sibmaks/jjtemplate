package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
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

        var expr = new FunctionCallTemplateExpression(
                function,
                List.of(new ConstantTemplateExpression(arg1), new ConstantTemplateExpression(arg2))
        );

        var folded = folder.visit(expr);

        var foldedValue = assertInstanceOf(ConstantTemplateExpression.class, folded);
        assertEquals(value, foldedValue.getValue());
    }

    @Test
    void foldStaticFunction_oneConstantArg() {
        TemplateFunction<String> function = mock();

        var arg1 = UUID.randomUUID().toString();
        var arg2 = UUID.randomUUID().toString();

        var expr = new FunctionCallTemplateExpression(
                function,
                List.of(
                        new VariableTemplateExpression(arg1, List.of()),
                        new TemplateConcatTemplateExpression(List.of(new ConstantTemplateExpression(arg2)))
                )
        );

        var folded = folder.visit(expr);

        var foldedFunction = assertInstanceOf(FunctionCallTemplateExpression.class, folded);
        assertNotSame(expr, folded);

        var argExpressions = foldedFunction.getArgExpressions();
        var foldedArg2 = assertInstanceOf(ConstantTemplateExpression.class, argExpressions.get(1));
        assertEquals(arg2, foldedArg2.getValue());
    }

    @Test
    void doNotFoldStaticFunctionWhenArgsNotConstant() {
        TemplateFunction<String> function = mock();

        var expr = new FunctionCallTemplateExpression(
                function,
                List.of(new VariableTemplateExpression("x", List.of()))
        );

        var folded = folder.visit(expr);

        assertSame(expr, folded);
    }

    @Test
    void doNotFoldDynamicFunctionEvenIfConstantArgs() {
        TemplateFunction<String> function = mock();
        when(function.isDynamic())
                .thenReturn(true);

        var expr = new FunctionCallTemplateExpression(
                function,
                List.of(new ConstantTemplateExpression(1))
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
                        new FunctionCallTemplateExpression(
                                function,
                                List.of(new ConstantTemplateExpression(arg))
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

        var arg1 = UUID.randomUUID().toString();
        var arg2 = UUID.randomUUID().toString();

        var expr = new PipeChainTemplateExpression(
                new FunctionCallTemplateExpression(
                        function,
                        List.of(
                                new VariableTemplateExpression(arg1, List.of()),
                                new TemplateConcatTemplateExpression(List.of(new ConstantTemplateExpression(arg2)))
                        )
                ),
                List.of()
        );

        var folded = folder.visit(expr);

        var foldedPipe = assertInstanceOf(PipeChainTemplateExpression.class, folded);
        assertNotSame(expr, folded);

        var foldedPipeRoot = assertInstanceOf(FunctionCallTemplateExpression.class, foldedPipe.getRoot());

        var argExpressions = foldedPipeRoot.getArgExpressions();
        var foldedArg2 = assertInstanceOf(ConstantTemplateExpression.class, argExpressions.get(1));
        assertEquals(arg2, foldedArg2.getValue());
    }

    @Test
    void doNotFoldPipe() {
        TemplateFunction<String> function = mock();

        var arg1 = UUID.randomUUID().toString();

        var expr = new PipeChainTemplateExpression(
                new FunctionCallTemplateExpression(
                        function,
                        List.of(
                                new VariableTemplateExpression(arg1, List.of())
                        )
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
                        new FunctionCallTemplateExpression(staticked, List.of(new ConstantTemplateExpression(arg))),
                        new FunctionCallTemplateExpression(dynamicked, List.of(new ConstantTemplateExpression(2)))
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

        var pipeArg = UUID.randomUUID().toString();
        var arg = UUID.randomUUID().toString();
        var value = UUID.randomUUID().toString();
        when(firstFunction.invoke(List.of(arg), pipeArg))
                .thenReturn(value);

        TemplateFunction<String> secondFunction = mock();

        var pipe = new PipeChainTemplateExpression(
                new ConstantTemplateExpression(pipeArg),
                List.of(
                        new FunctionCallTemplateExpression(firstFunction, List.of(new ConstantTemplateExpression(arg))),
                        new FunctionCallTemplateExpression(secondFunction, List.of(new VariableTemplateExpression("x", List.of())))
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
                        new FunctionCallTemplateExpression(staticked, List.of())
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
                        new FunctionCallTemplateExpression(staticked, List.of())
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
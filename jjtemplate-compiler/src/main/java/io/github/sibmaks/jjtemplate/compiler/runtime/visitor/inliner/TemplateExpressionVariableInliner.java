package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.FunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchTemplateExpression;

import java.util.ArrayList;
import java.util.Map;

/**
 * Performs variable inlining inside template expressions.
 * <p>
 * This visitor replaces occurrences of {@link VariableTemplateExpression}
 * with their constant values when such values are known at compile time.
 * <p>
 * Complex expressions (function calls, pipe chains, ternaries, concatenations)
 * are recursively traversed, and only the modified parts are rebuilt.
 *
 * <p>
 * Inlining happens only when:
 * <ul>
 *   <li>a variable exists in the provided value map,</li>
 *   <li>all function-call arguments in chained accesses inline into static values,</li>
 *   <li>property/method chains can be evaluated immediately.</li>
 * </ul>
 * If any part of a chain cannot be resolved statically, the original expression is preserved.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateExpressionVariableInliner implements TemplateExpressionVisitor<TemplateExpression> {
    private final Map<String, Object> values;
    private final ObjectElementVariableInliner objectElementVariableInliner;
    private final ListElementVariableInliner listElementVariableInliner;
    private final SwitchCaseVariableInliner switchCaseVariableInliner;

    /**
     * Creates an inliner with values available for static replacement.
     *
     * @param values map of variable names to constant values
     */
    public TemplateExpressionVariableInliner(Map<String, Object> values) {
        this.values = values;
        this.objectElementVariableInliner = new ObjectElementVariableInliner(this);
        this.listElementVariableInliner = new ListElementVariableInliner(this);
        this.switchCaseVariableInliner = new SwitchCaseVariableInliner(this);
    }

    @Override
    public TemplateExpression visit(DynamicFunctionCallTemplateExpression function) {
        var argExpression = function.getArgExpression();
        var foldedArgExpression = argExpression.visit(this);
        if (argExpression == foldedArgExpression) {
            return function;
        }
        return new DynamicFunctionCallTemplateExpression(function.getFunction(), (ListTemplateExpression) foldedArgExpression);
    }

    @Override
    public TemplateExpression visit(ConstantFunctionCallTemplateExpression expression) {
        return expression;
    }

    @Override
    public TemplateExpression visit(PipeChainTemplateExpression pipe) {
        var root = pipe.getRoot();
        var inlinedRoot = root.visit(this);
        var chain = new ArrayList<FunctionCallTemplateExpression>();
        var anyChainInlined = false;
        for (var pipeChainFunction : pipe.getChain()) {
            var inlined = pipeChainFunction.visit(this);
            anyChainInlined |= pipeChainFunction != inlined;
            chain.add((FunctionCallTemplateExpression) inlined);
        }

        if (root == inlinedRoot && !anyChainInlined) {
            return pipe;
        }
        return new PipeChainTemplateExpression(inlinedRoot, chain);
    }

    @Override
    public TemplateExpression visit(TemplateConcatTemplateExpression template) {
        var expressions = new ArrayList<TemplateExpression>();
        var anyInlined = false;
        for (var expression : template.getExpressions()) {
            var inlined = expression.visit(this);
            expressions.add(inlined);
            if (expression != inlined) {
                anyInlined = true;
            }
        }
        if (!anyInlined) {
            return template;
        }
        return new TemplateConcatTemplateExpression(expressions);
    }

    @Override
    public TemplateExpression visit(TernaryTemplateExpression ternary) {
        var condition = ternary.getCondition();
        var inlinedCondition = condition.visit(this);

        var thenTrue = ternary.getThenTrue();
        var inlinedThenTrue = thenTrue.visit(this);

        var thenFalse = ternary.getThenFalse();
        var inlinedThenFalse = thenFalse.visit(this);

        if (condition != inlinedCondition ||
                thenTrue != inlinedThenTrue ||
                thenFalse != inlinedThenFalse) {
            return new TernaryTemplateExpression(inlinedCondition, inlinedThenTrue, inlinedThenFalse);
        }
        return ternary;
    }

    @Override
    public TemplateExpression visit(ConstantTemplateExpression expression) {
        return expression;
    }

    @Override
    public TemplateExpression visit(VariableTemplateExpression variable) {
        var folding = true;
        var rootName = variable.getRootName();
        var value = values.get(rootName);
        if (value == null) {
            if (!values.containsKey(rootName)) {
                folding = false;
            } else {
                return new ConstantTemplateExpression(null);
            }
        }

        var callChain = variable.getCallChain();
        var newCallChain = new ArrayList<VariableTemplateExpression.Chain>();
        var anyArgInlined = false;
        for (var chain : callChain) {
            if (chain instanceof VariableTemplateExpression.CallMethodChain) {
                var callMethodChain = (VariableTemplateExpression.CallMethodChain) chain;
                var localAnyArgInlined = false;
                var argsExpressions = callMethodChain.getArgsExpressions();
                var inlinedArguments = new ArrayList<TemplateExpression>(argsExpressions.size());
                for (var argsExpression : argsExpressions) {
                    var inlined = argsExpression.visit(this);
                    localAnyArgInlined |= argsExpression != inlined;
                    inlinedArguments.add(inlined);
                    if (!(inlined instanceof ConstantTemplateExpression)) {
                        folding = false;
                    }
                }
                anyArgInlined |= localAnyArgInlined;
                if (localAnyArgInlined) {
                    callMethodChain = new VariableTemplateExpression.CallMethodChain(
                            callMethodChain.getMethodName(),
                            inlinedArguments
                    );
                }
                if (folding) {
                    value = callMethodChain.apply(Context.empty(), value);
                }
                newCallChain.add(callMethodChain);
            } else {
                var propertyChain = (VariableTemplateExpression.GetPropertyChain) chain;
                if (folding) {
                    value = propertyChain.apply(Context.empty(), value);
                }
                newCallChain.add(chain);
            }
        }

        if (folding) {
            return new ConstantTemplateExpression(value);
        }

        if (anyArgInlined) {
            return new VariableTemplateExpression(rootName, newCallChain);
        }

        return variable;
    }

    @Override
    public TemplateExpression visit(ListTemplateExpression expression) {
        var elements = expression.getElements();
        var inlinedElements = new ArrayList<ListElement>(elements.size());
        var anyInlined = false;
        for (var element : elements) {
            var inlinedElement = element.visit(listElementVariableInliner);
            var inlined = inlinedElement != element;
            anyInlined |= inlined;
            inlinedElements.add(inlinedElement);
        }
        if (!anyInlined) {
            return expression;
        }
        return new ListTemplateExpression(inlinedElements);
    }

    @Override
    public TemplateExpression visit(RangeTemplateExpression expression) {
        var source = expression.getSource();
        var inlinedSource = source.visit(this);
        var anyInlined = source != inlinedSource;

        var body = expression.getBody();
        var inlinedBody = body.visit(this);
        anyInlined |= body != inlinedBody;

        var name = expression.getName();
        var inlinedName = name.visit(this);
        anyInlined |= name != inlinedName;

        if (anyInlined) {
            return RangeTemplateExpression.builder()
                    .name(inlinedName)
                    .indexVariableName(expression.getIndexVariableName())
                    .itemVariableName(expression.getItemVariableName())
                    .source(inlinedSource)
                    .body(inlinedBody)
                    .build();
        }

        return expression;
    }

    @Override
    public TemplateExpression visit(ObjectTemplateExpression expression) {
        var elements = expression.getElements();
        var inlinedElements = new ArrayList<ObjectElement>(elements.size());
        var anyInlined = false;
        for (var element : elements) {
            var inlinedElement = element.visit(objectElementVariableInliner);
            var inlined = inlinedElement != element;
            anyInlined |= inlined;
            inlinedElements.add(inlinedElement);
        }
        if (!anyInlined) {
            return expression;
        }
        return new ObjectTemplateExpression(inlinedElements);
    }

    @Override
    public TemplateExpression visit(SwitchTemplateExpression expression) {
        var condition = expression.getCondition();
        var inlinedCondition = condition.visit(this);
        var anyInlined = condition != inlinedCondition;

        var cases = expression.getCases();
        var inlinedCases = new ArrayList<SwitchCase>(cases.size());

        for (var switchCase : cases) {
            var inlinedCase = switchCase.visit(switchCaseVariableInliner);
            anyInlined |= switchCase != inlinedCase;
            inlinedCases.add(inlinedCase);
        }

        if (anyInlined) {
            return SwitchTemplateExpression.builder()
                    .condition(inlinedCondition)
                    .cases(inlinedCases)
                    .build();
        }

        return expression;
    }
}
